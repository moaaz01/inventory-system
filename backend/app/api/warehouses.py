from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session, joinedload
from app.database import get_db
from app.models.warehouse import Warehouse
from app.models.stock import Stock
from app.schemas.warehouse import WarehouseCreate, WarehouseUpdate, WarehouseResponse, WarehouseDetailResponse, WarehouseStockItem
from app.api.deps import get_current_user, require_admin
from app.models.user import User
from app.models.product import Product

router = APIRouter(prefix="/api/warehouses", tags=["warehouses"])


@router.get("", response_model=list[WarehouseResponse])
def list_warehouses(db: Session = Depends(get_db), _: User = Depends(get_current_user)):
    return db.query(Warehouse).all()


@router.get("/{id}", response_model=WarehouseDetailResponse)
def get_warehouse(id: int, db: Session = Depends(get_db), _: User = Depends(get_current_user)):
    wh = db.query(Warehouse).options(
        joinedload(Warehouse.stock).joinedload(Stock.product)
    ).filter(Warehouse.id == id).first()
    if not wh:
        raise HTTPException(404, "Warehouse not found")
    # Build response manually to include stock items
    stock_items = [
        WarehouseStockItem(
            product_id=s.product_id,
            product_name=s.product.name if s.product else f"Product #{s.product_id}",
            quantity=s.quantity
        )
        for s in wh.stock
    ]
    return WarehouseDetailResponse(
        id=wh.id,
        name=wh.name,
        location=wh.location,
        manager_id=wh.manager_id,
        created_at=wh.created_at,
        stock_items=stock_items
    )


@router.post("", response_model=WarehouseResponse, status_code=201)
def create_warehouse(data: WarehouseCreate, db: Session = Depends(get_db), _: User = Depends(require_admin)):
    wh = Warehouse(**data.model_dump())
    db.add(wh)
    db.commit()
    db.refresh(wh)
    return wh


@router.put("/{id}", response_model=WarehouseResponse)
def update_warehouse(id: int, data: WarehouseUpdate, db: Session = Depends(get_db), _: User = Depends(require_admin)):
    wh = db.query(Warehouse).filter(Warehouse.id == id).first()
    if not wh:
        raise HTTPException(404, "Warehouse not found")
    for k, v in data.model_dump(exclude_unset=True).items():
        setattr(wh, k, v)
    db.commit()
    db.refresh(wh)
    return wh


@router.delete("/{id}", status_code=204)
def delete_warehouse(id: int, db: Session = Depends(get_db), _: User = Depends(require_admin)):
    wh = db.query(Warehouse).filter(Warehouse.id == id).first()
    if not wh:
        raise HTTPException(404, "Warehouse not found")
    stock_count = db.query(Stock).filter(Stock.warehouse_id == id, Stock.quantity > 0).count()
    if stock_count > 0:
        raise HTTPException(400, f"Cannot delete warehouse: it has {stock_count} stock entries with quantity > 0. Transfer or clear stock first.")
    db.delete(wh)
    db.commit()
