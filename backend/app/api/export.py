import csv
import io
import zipfile
from fastapi import APIRouter, Depends, HTTPException, UploadFile, File
from fastapi.responses import StreamingResponse
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.product import Product
from app.models.warehouse import Warehouse
from app.models.stock import Stock
from app.api.deps import require_admin, get_current_user
from app.models.user import User

router = APIRouter(tags=["export/import"])


# ─── EXPORT ───────────────────────────────────────────────────────────────────

@router.get("/api/export/products")
def export_products(db: Session = Depends(get_db), _: User = Depends(get_current_user)):
    products = db.query(Product).all()
    output = io.StringIO()
    writer = csv.writer(output)
    writer.writerow(["id", "sku", "name", "category_id", "unit_id", "min_stock_level", "barcode", "description"])
    for p in products:
        writer.writerow([p.id, p.sku, p.name, p.category_id, p.unit_id, p.min_stock_level, p.barcode or "", p.description or ""])
    output.seek(0)
    return StreamingResponse(iter([output.getvalue()]), media_type="text/csv",
                             headers={"Content-Disposition": "attachment; filename=products.csv"})


@router.get("/api/export/warehouses")
def export_warehouses(db: Session = Depends(get_db), _: User = Depends(get_current_user)):
    warehouses = db.query(Warehouse).all()
    output = io.StringIO()
    writer = csv.writer(output)
    writer.writerow(["id", "name", "location", "manager_id"])
    for w in warehouses:
        writer.writerow([w.id, w.name, w.location or "", w.manager_id or ""])
    output.seek(0)
    return StreamingResponse(iter([output.getvalue()]), media_type="text/csv",
                             headers={"Content-Disposition": "attachment; filename=warehouses.csv"})


@router.get("/api/export/stock")
def export_stock(db: Session = Depends(get_db), _: User = Depends(get_current_user)):
    stocks = db.query(Stock).all()
    output = io.StringIO()
    writer = csv.writer(output)
    writer.writerow(["id", "product_id", "warehouse_id", "quantity"])
    for s in stocks:
        writer.writerow([s.id, s.product_id, s.warehouse_id, s.quantity])
    output.seek(0)
    return StreamingResponse(iter([output.getvalue()]), media_type="text/csv",
                             headers={"Content-Disposition": "attachment; filename=stock.csv"})


@router.get("/api/export/all")
def export_all(db: Session = Depends(get_db), _: User = Depends(get_current_user)):
    def make_csv_bytes(rows, headers):
        buf = io.StringIO()
        writer = csv.writer(buf)
        writer.writerow(headers)
        writer.writerows(rows)
        return buf.getvalue().encode()

    products_csv = make_csv_bytes(
        [[p.id, p.sku, p.name, p.category_id, p.unit_id, p.min_stock_level, p.barcode or "", p.description or ""]
         for p in db.query(Product).all()],
        ["id", "sku", "name", "category_id", "unit_id", "min_stock_level", "barcode", "description"]
    )
    warehouses_csv = make_csv_bytes(
        [[w.id, w.name, w.location or "", w.manager_id or ""] for w in db.query(Warehouse).all()],
        ["id", "name", "location", "manager_id"]
    )
    stock_csv = make_csv_bytes(
        [[s.id, s.product_id, s.warehouse_id, s.quantity] for s in db.query(Stock).all()],
        ["id", "product_id", "warehouse_id", "quantity"]
    )

    zip_buf = io.BytesIO()
    with zipfile.ZipFile(zip_buf, "w", zipfile.ZIP_DEFLATED) as zf:
        zf.writestr("products.csv", products_csv)
        zf.writestr("warehouses.csv", warehouses_csv)
        zf.writestr("stock.csv", stock_csv)
    zip_buf.seek(0)
    return StreamingResponse(iter([zip_buf.read()]), media_type="application/zip",
                             headers={"Content-Disposition": "attachment; filename=inventory_export.zip"})


# ─── IMPORT ───────────────────────────────────────────────────────────────────

@router.post("/api/import/products")
def import_products(file: UploadFile = File(...), db: Session = Depends(get_db), _: User = Depends(require_admin)):
    content = file.file.read().decode("utf-8")
    reader = csv.DictReader(io.StringIO(content))
    created = updated = failed = 0
    errors = []
    for i, row in enumerate(reader, start=2):
        try:
            row_id = row.get("id", "").strip()
            if row_id and row_id.isdigit():
                existing = db.query(Product).filter(Product.id == int(row_id)).first()
                if existing:
                    if "sku" in row and row["sku"]: existing.sku = row["sku"].strip()
                    if "name" in row and row["name"]: existing.name = row["name"].strip()
                    if "category_id" in row and row["category_id"]: existing.category_id = int(row["category_id"]) if row["category_id"].strip().isdigit() else existing.category_id
                    if "unit_id" in row and row["unit_id"]: existing.unit_id = int(row["unit_id"]) if row["unit_id"].strip().isdigit() else existing.unit_id
                    if "min_stock_level" in row and row["min_stock_level"]: existing.min_stock_level = int(row["min_stock_level"]) if row["min_stock_level"].strip().isdigit() else existing.min_stock_level
                    updated += 1
                    continue
            # Create new
            sku = row.get("sku", "").strip()
            name = row.get("name", "").strip()
            if not sku or not name:
                raise ValueError("sku and name are required")
            if db.query(Product).filter(Product.sku == sku).first():
                raise ValueError(f"SKU '{sku}' already exists")
            p = Product(
                sku=sku,
                name=name,
                category_id=int(row["category_id"]) if row.get("category_id", "").strip().isdigit() else None,
                unit_id=int(row["unit_id"]) if row.get("unit_id", "").strip().isdigit() else None,
                min_stock_level=int(row["min_stock_level"]) if row.get("min_stock_level", "").strip().isdigit() else 10,
            )
            db.add(p)
            created += 1
        except Exception as e:
            failed += 1
            errors.append({"row": i, "error": str(e)})
    db.commit()
    return {"created": created, "updated": updated, "failed": failed, "errors": errors}


@router.post("/api/import/warehouses")
def import_warehouses(file: UploadFile = File(...), db: Session = Depends(get_db), _: User = Depends(require_admin)):
    content = file.file.read().decode("utf-8")
    reader = csv.DictReader(io.StringIO(content))
    created = updated = failed = 0
    errors = []
    for i, row in enumerate(reader, start=2):
        try:
            row_id = row.get("id", "").strip()
            if row_id and row_id.isdigit():
                existing = db.query(Warehouse).filter(Warehouse.id == int(row_id)).first()
                if existing:
                    if "name" in row and row["name"]: existing.name = row["name"].strip()
                    if "location" in row: existing.location = row["location"].strip() or None
                    updated += 1
                    continue
            name = row.get("name", "").strip()
            if not name:
                raise ValueError("name is required")
            w = Warehouse(name=name, location=row.get("location", "").strip() or None)
            db.add(w)
            created += 1
        except Exception as e:
            failed += 1
            errors.append({"row": i, "error": str(e)})
    db.commit()
    return {"created": created, "updated": updated, "failed": failed, "errors": errors}


@router.post("/api/import/stock")
def import_stock(file: UploadFile = File(...), db: Session = Depends(get_db), _: User = Depends(require_admin)):
    content = file.file.read().decode("utf-8")
    reader = csv.DictReader(io.StringIO(content))
    created = updated = failed = 0
    errors = []
    for i, row in enumerate(reader, start=2):
        try:
            row_id = row.get("id", "").strip()
            if row_id and row_id.isdigit():
                existing = db.query(Stock).filter(Stock.id == int(row_id)).first()
                if existing:
                    if "quantity" in row: existing.quantity = int(row["quantity"])
                    updated += 1
                    continue
            product_id = int(row["product_id"])
            warehouse_id = int(row["warehouse_id"])
            quantity = int(row.get("quantity", 0))
            existing = db.query(Stock).filter(Stock.product_id == product_id, Stock.warehouse_id == warehouse_id).first()
            if existing:
                existing.quantity = quantity
                updated += 1
            else:
                db.add(Stock(product_id=product_id, warehouse_id=warehouse_id, quantity=quantity))
                created += 1
        except Exception as e:
            failed += 1
            errors.append({"row": i, "error": str(e)})
    db.commit()
    return {"created": created, "updated": updated, "failed": failed, "errors": errors}
