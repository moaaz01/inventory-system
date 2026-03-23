from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.invoice import Invoice, InvoiceItem
from app.models.product import Product
from app.models.stock import Stock
from app.models.movement import StockMovement
from app.schemas.invoice import InvoiceCreate, InvoiceResponse, InvoiceItemResponse
from app.api.deps import get_current_user
from app.models.user import User
from datetime import datetime
import uuid

router = APIRouter(prefix="/api/invoices", tags=["invoices"])

def generate_invoice_number():
    now = datetime.now()
    return f"INV-{now.strftime('%Y%m%d')}-{uuid.uuid4().hex[:6].upper()}"

@router.get("", response_model=list[InvoiceResponse])
def list_invoices(
    skip: int = 0,
    limit: int = 50,
    start_date: str = None,
    end_date: str = None,
    search: str = None,
    db: Session = Depends(get_db),
    _: User = Depends(get_current_user)
):
    q = db.query(Invoice)
    if start_date:
        q = q.filter(Invoice.created_at >= start_date)
    if end_date:
        q = q.filter(Invoice.created_at <= end_date)
    if search:
        q = q.filter(
            (Invoice.invoice_number.ilike(f"%{search}%")) |
            (Invoice.customer_name.ilike(f"%{search}%"))
        )
    return q.order_by(Invoice.created_at.desc()).offset(skip).limit(limit).all()

@router.post("", response_model=InvoiceResponse)
def create_invoice(data: InvoiceCreate, db: Session = Depends(get_db), user: User = Depends(get_current_user)):
    subtotal = sum(item.quantity * item.unit_price for item in data.items)
    total = subtotal - data.discount

    invoice = Invoice(
        invoice_number=generate_invoice_number(),
        customer_name=data.customer_name,
        subtotal=subtotal,
        discount=data.discount,
        total=total,
        created_by=user.id
    )
    db.add(invoice)
    db.flush()

    for item in data.items:
        product = db.query(Product).filter(Product.id == item.product_id).first()
        invoice_item = InvoiceItem(
            invoice_id=invoice.id,
            product_id=item.product_id,
            product_name=product.name if product else "Unknown",
            product_sku=product.sku if product else "",
            quantity=item.quantity,
            unit_price=item.unit_price,
            total_price=item.quantity * item.unit_price
        )
        db.add(invoice_item)

        # Deduct stock: pick the warehouse with the most available quantity
        if item.product_id:
            stock_entry = (
                db.query(Stock)
                .filter(Stock.product_id == item.product_id, Stock.quantity > 0)
                .order_by(Stock.quantity.desc())
                .first()
            )
            if stock_entry:
                qty_int = int(item.quantity)
                stock_entry.quantity = max(0, stock_entry.quantity - qty_int)
                db.add(StockMovement(
                    product_id=item.product_id,
                    warehouse_id=stock_entry.warehouse_id,
                    movement_type="issue",
                    quantity=qty_int,
                    reference_number=invoice.invoice_number,
                    notes=f"فاتورة مبيعات - {invoice.invoice_number}",
                    user_id=user.id,
                ))

    db.commit()
    db.refresh(invoice)
    return invoice

@router.get("/by-number/{invoice_number}", response_model=InvoiceResponse)
def get_invoice_by_number(invoice_number: str, db: Session = Depends(get_db), _: User = Depends(get_current_user)):
    invoice = db.query(Invoice).filter(Invoice.invoice_number == invoice_number).first()
    if not invoice:
        raise HTTPException(404, "Invoice not found")
    return invoice

@router.get("/{invoice_id}", response_model=InvoiceResponse)
def get_invoice(invoice_id: int, db: Session = Depends(get_db), _: User = Depends(get_current_user)):
    invoice = db.query(Invoice).filter(Invoice.id == invoice_id).first()
    if not invoice:
        raise HTTPException(404, "Invoice not found")
    return invoice

@router.post("/{invoice_id}/cancel", response_model=InvoiceResponse)
def cancel_invoice(invoice_id: int, db: Session = Depends(get_db), user: User = Depends(get_current_user)):
    invoice = db.query(Invoice).filter(Invoice.id == invoice_id).first()
    if not invoice:
        raise HTTPException(404, "Invoice not found")
    if invoice.status == "cancelled":
        raise HTTPException(400, "Invoice already cancelled")

    invoice.status = "cancelled"

    # Restore stock for each item
    for item in invoice.items:
        if not item.product_id:
            continue
        stock_entry = (
            db.query(Stock)
            .filter(Stock.product_id == item.product_id)
            .order_by(Stock.quantity.desc())
            .first()
        )
        if stock_entry:
            qty_int = int(item.quantity)
            stock_entry.quantity += qty_int
            db.add(StockMovement(
                product_id=item.product_id,
                warehouse_id=stock_entry.warehouse_id,
                movement_type="receipt",
                quantity=qty_int,
                reference_number=invoice.invoice_number,
                notes=f"إلغاء فاتورة - {invoice.invoice_number}",
                user_id=user.id,
            ))

    db.commit()
    db.refresh(invoice)
    return invoice
