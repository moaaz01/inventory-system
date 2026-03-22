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
from app.models.category import Category
from app.models.unit import Unit
from app.api.deps import require_admin, get_current_user
from app.models.user import User

router = APIRouter(tags=["export/import"])


# ─── EXPORT ───────────────────────────────────────────────────────────────────

@router.get("/api/export/products")
def export_products(db: Session = Depends(get_db), _: User = Depends(get_current_user)):
    products = db.query(Product).all()
    output = io.StringIO()
    writer = csv.writer(output)
    writer.writerow(["id", "sku", "name", "category_id", "category_name", "unit_id", "unit_name", "min_stock_level", "barcode", "description"])
    for p in products:
        writer.writerow([
            p.id, p.sku, p.name,
            p.category_id, p.category.name if p.category else "",
            p.unit_id, p.unit.name if p.unit else "",
            p.min_stock_level, p.barcode or "", p.description or ""
        ])
    output.seek(0)
    # UTF-8 BOM for Excel Arabic support
    content = output.getvalue()
    return StreamingResponse(
        iter([("\ufeff" + content).encode("utf-8-sig")]),
        media_type="text/csv; charset=utf-8-sig",
        headers={"Content-Disposition": "attachment; filename=products.csv"}
    )


@router.get("/api/export/warehouses")
def export_warehouses(db: Session = Depends(get_db), _: User = Depends(get_current_user)):
    warehouses = db.query(Warehouse).all()
    output = io.StringIO()
    writer = csv.writer(output)
    writer.writerow(["id", "name", "location", "manager_id"])
    for w in warehouses:
        writer.writerow([w.id, w.name, w.location or "", w.manager_id or ""])
    output.seek(0)
    content = "\ufeff" + output.getvalue()
    return StreamingResponse(
        iter([content.encode("utf-8-sig")]),
        media_type="text/csv; charset=utf-8",
        headers={"Content-Disposition": "attachment; filename=warehouses.csv"}
    )


@router.get("/api/export/stock")
def export_stock(db: Session = Depends(get_db), _: User = Depends(get_current_user)):
    stocks = db.query(Stock).all()
    output = io.StringIO()
    writer = csv.writer(output)
    writer.writerow(["id", "product_id", "warehouse_id", "quantity"])
    for s in stocks:
        writer.writerow([s.id, s.product_id, s.warehouse_id, s.quantity])
    output.seek(0)
    content = "\ufeff" + output.getvalue()
    return StreamingResponse(
        iter([content.encode("utf-8-sig")]),
        media_type="text/csv; charset=utf-8",
        headers={"Content-Disposition": "attachment; filename=stock.csv"}
    )


@router.get("/api/export/all")
def export_all(db: Session = Depends(get_db), _: User = Depends(get_current_user)):
    def make_csv_bytes(rows, headers):
        buf = io.StringIO()
        writer = csv.writer(buf)
        writer.writerow(headers)
        writer.writerows(rows)
        # UTF-8 with BOM for Arabic
        return ("\ufeff" + buf.getvalue()).encode("utf-8-sig")

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
    raw = file.file.read()
    # Handle UTF-8 BOM
    content = raw.decode("utf-8-sig")
    reader = csv.DictReader(io.StringIO(content))
    # Strip BOM from field names if present (extra safety)
    fieldnames = reader.fieldnames
    if fieldnames:
        reader.fieldnames = [f.lstrip("\ufeff").strip() for f in fieldnames]

    # Build lookup caches
    categories = {c.name.strip().lower(): c.id for c in db.query(Category).all()}
    units = {u.name.strip().lower(): u.id for u in db.query(Unit).all()}

    created = updated = failed = 0
    errors = []
    for i, row in enumerate(reader, start=2):
        try:
            # Strip all values
            row = {k: (v.strip() if v else "") for k, v in row.items()}

            # Resolve category_id: prefer name, fall back to id column
            category_id = None
            cat_name = row.get("category_name", "")
            cat_id_raw = row.get("category_id", "")
            if cat_name and cat_name.lower() in categories:
                category_id = categories[cat_name.lower()]
            elif cat_id_raw.isdigit():
                category_id = int(cat_id_raw)

            # Resolve unit_id: prefer name, fall back to id column
            unit_id = None
            unit_name = row.get("unit_name", "")
            unit_id_raw = row.get("unit_id", "")
            if unit_name and unit_name.lower() in units:
                unit_id = units[unit_name.lower()]
            elif unit_id_raw.isdigit():
                unit_id = int(unit_id_raw)

            row_id = row.get("id", "")
            if row_id and row_id.isdigit():
                existing = db.query(Product).filter(Product.id == int(row_id)).first()
                if existing:
                    if row.get("sku"): existing.sku = row["sku"]
                    if row.get("name"): existing.name = row["name"]
                    if category_id is not None: existing.category_id = category_id
                    if unit_id is not None: existing.unit_id = unit_id
                    if row.get("min_stock_level") and row["min_stock_level"].isdigit():
                        existing.min_stock_level = int(row["min_stock_level"])
                    if row.get("description"): existing.description = row["description"]
                    if row.get("barcode"): existing.barcode = row["barcode"]
                    updated += 1
                    continue

            sku = row.get("sku", "")
            name = row.get("name", "")
            if not sku or not name:
                raise ValueError("sku and name are required")
            if db.query(Product).filter(Product.sku == sku).first():
                raise ValueError(f"SKU '{sku}' already exists")
            p = Product(
                sku=sku,
                name=name,
                description=row.get("description") or None,
                barcode=row.get("barcode") or None,
                category_id=category_id,
                unit_id=unit_id,
                min_stock_level=int(row["min_stock_level"]) if row.get("min_stock_level", "").isdigit() else 10,
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
    content = file.file.read().decode("utf-8-sig")
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
    content = file.file.read().decode("utf-8-sig")
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


@router.get("/api/barcode/{sku}")
def generate_barcode(sku: str, db: Session = Depends(get_db), _: User = Depends(get_current_user)):
    """Generate barcode PNG for a SKU."""
    try:
        import barcode
        from barcode.writer import ImageWriter
        from io import BytesIO
        
        code128 = barcode.get_barcode_class('code128')
        rv = code128(sku, writer=ImageWriter())
        buffer = BytesIO()
        rv.write(buffer)
        buffer.seek(0)
        
        from fastapi.responses import Response
        return Response(content=buffer.getvalue(), media_type="image/png",
                       headers={"Content-Disposition": f"inline; filename={sku}.png"})
    except Exception as e:
        raise HTTPException(500, f"Failed to generate barcode: {str(e)}")
