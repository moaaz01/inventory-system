from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from sqlalchemy import func
from typing import Optional
from datetime import datetime
from app.database import get_db
from app.models.product import Product
from app.models.stock import Stock
from app.models.movement import StockMovement
from app.models.warehouse import Warehouse
from app.api.deps import get_current_user, require_admin
from app.models.user import User

router = APIRouter(tags=["reports-dashboard"])


@router.get("/api/reports/inventory")
def inventory_report(db: Session = Depends(get_db), _: User = Depends(get_current_user)):
    stocks = db.query(Stock).all()
    result = []
    for s in stocks:
        result.append({
            "product_id": s.product_id,
            "product_name": s.product.name,
            "sku": s.product.sku,
            "warehouse_id": s.warehouse_id,
            "warehouse_name": s.warehouse.name,
            "quantity": s.quantity,
            "min_stock_level": s.product.min_stock_level,
            "is_low_stock": s.quantity < s.product.min_stock_level,
        })
    return result


@router.get("/api/reports/low-stock")
def low_stock_report(db: Session = Depends(get_db), _: User = Depends(get_current_user)):
    stocks = db.query(Stock).all()
    return [
        {
            "product_id": s.product_id,
            "product_name": s.product.name,
            "sku": s.product.sku,
            "warehouse_id": s.warehouse_id,
            "warehouse_name": s.warehouse.name,
            "quantity": s.quantity,
            "min_stock_level": s.product.min_stock_level,
        }
        for s in stocks
        if s.quantity < s.product.min_stock_level
    ]


@router.get("/api/reports/movements")
def movements_report(
    from_date: Optional[datetime] = None,
    to_date: Optional[datetime] = None,
    movement_type: Optional[str] = None,
    db: Session = Depends(get_db),
    _: User = Depends(get_current_user),
):
    q = db.query(StockMovement)
    if from_date:
        q = q.filter(StockMovement.created_at >= from_date)
    if to_date:
        q = q.filter(StockMovement.created_at <= to_date)
    if movement_type:
        q = q.filter(StockMovement.movement_type == movement_type)
    movements = q.order_by(StockMovement.created_at.desc()).all()
    return [
        {
            "id": m.id,
            "product": m.product.name,
            "warehouse": m.warehouse.name,
            "type": m.movement_type,
            "quantity": m.quantity,
            "reference": m.reference_number,
            "date": m.created_at,
        }
        for m in movements
    ]


@router.get("/api/dashboard/stats")
def dashboard_stats(db: Session = Depends(get_db), _: User = Depends(require_admin)):
    total_products = db.query(Product).count()
    total_warehouses = db.query(Warehouse).count()
    stocks = db.query(Stock).all()
    low_stock_count = sum(1 for s in stocks if s.quantity < s.product.min_stock_level)
    recent_movements = db.query(StockMovement).count()
    return {
        "total_products": total_products,
        "total_warehouses": total_warehouses,
        "total_stock_value": sum(s.quantity for s in stocks),
        "low_stock_count": low_stock_count,
        "recent_movements": recent_movements,
    }
