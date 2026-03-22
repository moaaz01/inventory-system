from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import Optional
from datetime import datetime
from app.database import get_db
from app.models.stock import Stock
from app.models.movement import StockMovement
from app.models.product import Product
from app.models.warehouse import Warehouse
from app.schemas.movement import (
    MovementResponse, MovementListResponse,
    ReceiptRequest, IssueRequest, TransferRequest,
)
from app.api.deps import get_current_user, require_admin
from app.models.user import User

router = APIRouter(prefix="/api/movements", tags=["movements"])


def to_movement_response(m: StockMovement) -> MovementResponse:
    return MovementResponse(
        id=m.id,
        product_id=m.product_id,
        product_name=m.product.name,
        warehouse_id=m.warehouse_id,
        warehouse_name=m.warehouse.name,
        to_warehouse_id=m.to_warehouse_id,
        movement_type=m.movement_type,
        quantity=m.quantity,
        reference_number=m.reference_number,
        notes=m.notes,
        user_id=m.user_id,
        created_at=m.created_at,
    )


def get_or_create_stock(db: Session, product_id: int, warehouse_id: int) -> Stock:
    stock = db.query(Stock).filter(Stock.product_id == product_id, Stock.warehouse_id == warehouse_id).first()
    if not stock:
        stock = Stock(product_id=product_id, warehouse_id=warehouse_id, quantity=0)
        db.add(stock)
        db.flush()
    return stock


@router.get("", response_model=MovementListResponse)
def list_movements(
    movement_type: Optional[str] = None,
    product_id: Optional[int] = None,
    warehouse_id: Optional[int] = None,
    from_date: Optional[datetime] = None,
    to_date: Optional[datetime] = None,
    page: int = Query(1, ge=1),
    size: int = Query(20, ge=1, le=100),
    db: Session = Depends(get_db),
    _: User = Depends(get_current_user),
):
    q = db.query(StockMovement)
    if movement_type:
        q = q.filter(StockMovement.movement_type == movement_type)
    if product_id:
        q = q.filter(StockMovement.product_id == product_id)
    if warehouse_id:
        q = q.filter(StockMovement.warehouse_id == warehouse_id)
    if from_date:
        q = q.filter(StockMovement.created_at >= from_date)
    if to_date:
        q = q.filter(StockMovement.created_at <= to_date)
    total = q.count()
    items = q.order_by(StockMovement.created_at.desc()).offset((page - 1) * size).limit(size).all()
    return MovementListResponse(items=[to_movement_response(m) for m in items], total=total, page=page, size=size)


@router.post("/receipt", response_model=MovementResponse, status_code=201)
def receipt(data: ReceiptRequest, db: Session = Depends(get_db), current_user: User = Depends(require_admin)):
    if not db.query(Product).filter(Product.id == data.product_id).first():
        raise HTTPException(404, "Product not found")
    if not db.query(Warehouse).filter(Warehouse.id == data.warehouse_id).first():
        raise HTTPException(404, "Warehouse not found")

    stock = get_or_create_stock(db, data.product_id, data.warehouse_id)
    stock.quantity += data.quantity

    movement = StockMovement(
        product_id=data.product_id,
        warehouse_id=data.warehouse_id,
        movement_type="receipt",
        quantity=data.quantity,
        reference_number=data.reference_number,
        notes=data.notes,
        user_id=current_user.id,
    )
    db.add(movement)
    db.commit()
    db.refresh(movement)
    return to_movement_response(movement)


@router.post("/issue", response_model=MovementResponse, status_code=201)
def issue(data: IssueRequest, db: Session = Depends(get_db), current_user: User = Depends(require_admin)):
    if not db.query(Product).filter(Product.id == data.product_id).first():
        raise HTTPException(404, "Product not found")
    stock = get_or_create_stock(db, data.product_id, data.warehouse_id)
    if stock.quantity < data.quantity:
        raise HTTPException(400, f"Insufficient stock. Available: {stock.quantity}")
    stock.quantity -= data.quantity

    movement = StockMovement(
        product_id=data.product_id,
        warehouse_id=data.warehouse_id,
        movement_type="issue",
        quantity=data.quantity,
        reference_number=data.reference_number,
        notes=data.notes,
        user_id=current_user.id,
    )
    db.add(movement)
    db.commit()
    db.refresh(movement)
    return to_movement_response(movement)


@router.post("/transfer", response_model=MovementResponse, status_code=201)
def transfer(data: TransferRequest, db: Session = Depends(get_db), current_user: User = Depends(require_admin)):
    if data.from_warehouse_id == data.to_warehouse_id:
        raise HTTPException(400, "Source and destination warehouses must differ")
    if not db.query(Product).filter(Product.id == data.product_id).first():
        raise HTTPException(404, "Product not found")
    if not db.query(Warehouse).filter(Warehouse.id == data.to_warehouse_id).first():
        raise HTTPException(404, "Destination warehouse not found")

    from_stock = get_or_create_stock(db, data.product_id, data.from_warehouse_id)
    if from_stock.quantity < data.quantity:
        raise HTTPException(400, f"Insufficient stock. Available: {from_stock.quantity}")
    from_stock.quantity -= data.quantity

    to_stock = get_or_create_stock(db, data.product_id, data.to_warehouse_id)
    to_stock.quantity += data.quantity

    movement = StockMovement(
        product_id=data.product_id,
        warehouse_id=data.from_warehouse_id,
        to_warehouse_id=data.to_warehouse_id,
        movement_type="transfer",
        quantity=data.quantity,
        notes=data.notes,
        user_id=current_user.id,
    )
    db.add(movement)
    db.commit()
    db.refresh(movement)
    return to_movement_response(movement)
