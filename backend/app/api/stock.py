from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import Optional
from app.database import get_db
from app.models.stock import Stock
from app.models.product import Product
from app.models.warehouse import Warehouse
from app.models.movement import StockMovement
from app.schemas.stock import StockResponse, StockAdjust
from app.api.deps import get_current_user, require_admin
from app.models.user import User

router = APIRouter(prefix="/api/stock", tags=["stock"])


def to_stock_response(s: Stock) -> StockResponse:
    return StockResponse(
        id=s.id,
        product_id=s.product_id,
        product_name=s.product.name,
        warehouse_id=s.warehouse_id,
        warehouse_name=s.warehouse.name,
        quantity=s.quantity,
        min_stock_level=s.product.min_stock_level,
        is_low_stock=s.quantity < s.product.min_stock_level,
        updated_at=s.updated_at,
    )


@router.get("", response_model=list[StockResponse])
def list_stock(
    warehouse_id: Optional[int] = None,
    product_id: Optional[int] = None,
    low_stock: bool = False,
    db: Session = Depends(get_db),
    _: User = Depends(get_current_user),
):
    q = db.query(Stock)
    if warehouse_id:
        q = q.filter(Stock.warehouse_id == warehouse_id)
    if product_id:
        q = q.filter(Stock.product_id == product_id)
    items = q.all()
    result = [to_stock_response(s) for s in items]
    if low_stock:
        result = [s for s in result if s.is_low_stock]
    return result


@router.put("/{product_id}/{warehouse_id}", response_model=StockResponse)
def adjust_stock(
    product_id: int,
    warehouse_id: int,
    data: StockAdjust,
    db: Session = Depends(get_db),
    current_user: User = Depends(require_admin),
):
    product = db.query(Product).filter(Product.id == product_id).first()
    if not product:
        raise HTTPException(404, "Product not found")
    warehouse = db.query(Warehouse).filter(Warehouse.id == warehouse_id).first()
    if not warehouse:
        raise HTTPException(404, "Warehouse not found")

    stock = db.query(Stock).filter(Stock.product_id == product_id, Stock.warehouse_id == warehouse_id).first()
    if not stock:
        stock = Stock(product_id=product_id, warehouse_id=warehouse_id, quantity=0)
        db.add(stock)
        db.flush()

    old_qty = stock.quantity
    stock.quantity = data.quantity
    db.flush()

    # Record adjustment movement
    diff = data.quantity - old_qty
    if diff != 0:
        movement = StockMovement(
            product_id=product_id,
            warehouse_id=warehouse_id,
            movement_type="adjustment",
            quantity=abs(diff),
            notes=data.notes or f"Manual adjustment from {old_qty} to {data.quantity}",
            user_id=current_user.id,
        )
        db.add(movement)

    db.commit()
    db.refresh(stock)
    return to_stock_response(stock)
