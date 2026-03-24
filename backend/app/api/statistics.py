from fastapi import APIRouter, Depends, Query, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy import func
from datetime import datetime, timedelta
from typing import Optional, List, Dict

from app.database import get_db
from app.api.deps import require_admin
from app.models.invoice import Invoice, InvoiceItem
from app.models.product import Product
from app.models.stock import Stock

router = APIRouter(prefix="/api/statistics", tags=["Statistics"])


@router.get("/sales/daily")
def get_daily_sales(
    date: str = Query(None, description="Date in YYYY-MM-DD format (default: today)"),
    db: Session = Depends(get_db),
    current_user = Depends(require_admin)
):
    if not date:
        date = datetime.now().strftime("%Y-%m-%d")
    
    try:
        target_date = datetime.strptime(date, "%Y-%m-%d").date()
    except ValueError:
        raise HTTPException(status_code=400, detail="Invalid date format. Use YYYY-MM-DD")
    
    # Get sales totals
    sales = db.query(
        func.coalesce(func.sum(Invoice.total), 0).label("total_sales"),
        func.count(Invoice.id).label("total_invoices")
    ).filter(
        func.date(Invoice.created_at) == target_date,
        Invoice.status == "completed"
    ).first()
    
    # Get sales by currency from invoice items
    currency_sales = db.query(
        Product.currency,
        func.coalesce(func.sum(InvoiceItem.total_price), 0).label("total")
    ).join(InvoiceItem, InvoiceItem.product_id == Product.id
    ).join(Invoice, Invoice.id == InvoiceItem.invoice_id
    ).filter(
        func.date(Invoice.created_at) == target_date,
        Invoice.status == "completed",
        Product.currency.isnot(None)
    ).group_by(Product.currency).all()
    
    # Get top products
    top_products = db.query(
        Product.id,
        Product.name,
        Product.sku,
        func.coalesce(func.sum(InvoiceItem.quantity), 0).label("total_quantity"),
        func.coalesce(func.sum(InvoiceItem.total_price), 0).label("total_revenue")
    ).join(InvoiceItem, InvoiceItem.product_id == Product.id
    ).join(Invoice, Invoice.id == InvoiceItem.invoice_id
    ).filter(
        func.date(Invoice.created_at) == target_date,
        Invoice.status == "completed"
    ).group_by(Product.id, Product.name, Product.sku
    ).order_by(func.sum(InvoiceItem.quantity).desc()
    ).limit(10).all()
    
    return {
        "date": date,
        "total_sales": float(sales.total_sales or 0),
        "total_invoices": sales.total_invoices or 0,
        "by_currency": {row[0]: float(row[1]) for row in currency_sales if row[0]},
        "top_products": [
            {
                "id": p.id,
                "name": p.name,
                "sku": p.sku,
                "total_quantity": float(p.total_quantity),
                "total_revenue": float(p.total_revenue)
            }
            for p in top_products
        ]
    }


@router.get("/sales/monthly")
def get_monthly_sales(
    year: int = Query(None),
    month: int = Query(None, ge=1, le=12),
    db: Session = Depends(get_db),
    current_user = Depends(require_admin)
):
    if not year:
        year = datetime.now().year
    if not month:
        month = datetime.now().month
    
    # Get monthly totals
    sales = db.query(
        func.coalesce(func.sum(Invoice.total), 0).label("total_sales"),
        func.count(Invoice.id).label("total_invoices")
    ).filter(
        func.extract("year", Invoice.created_at) == year,
        func.extract("month", Invoice.created_at) == month,
        Invoice.status == "completed"
    ).first()
    
    # Get sales by currency
    currency_sales = db.query(
        Product.currency,
        func.coalesce(func.sum(InvoiceItem.total_price), 0).label("total")
    ).join(InvoiceItem, InvoiceItem.product_id == Product.id
    ).join(Invoice, Invoice.id == InvoiceItem.invoice_id
    ).filter(
        func.extract("year", Invoice.created_at) == year,
        func.extract("month", Invoice.created_at) == month,
        Invoice.status == "completed",
        Product.currency.isnot(None)
    ).group_by(Product.currency).all()
    
    # Get daily breakdown
    daily_breakdown = db.query(
        func.date(Invoice.created_at).label("date"),
        func.coalesce(func.sum(Invoice.total), 0).label("total"),
        func.count(Invoice.id).label("count")
    ).filter(
        func.extract("year", Invoice.created_at) == year,
        func.extract("month", Invoice.created_at) == month,
        Invoice.status == "completed"
    ).group_by(func.date(Invoice.created_at)
    ).order_by(func.date(Invoice.created_at)).all()
    
    # Get top products for the month
    top_products = db.query(
        Product.id,
        Product.name,
        Product.sku,
        func.coalesce(func.sum(InvoiceItem.quantity), 0).label("total_quantity"),
        func.coalesce(func.sum(InvoiceItem.total_price), 0).label("total_revenue")
    ).join(InvoiceItem, InvoiceItem.product_id == Product.id
    ).join(Invoice, Invoice.id == InvoiceItem.invoice_id
    ).filter(
        func.extract("year", Invoice.created_at) == year,
        func.extract("month", Invoice.created_at) == month,
        Invoice.status == "completed"
    ).group_by(Product.id, Product.name, Product.sku
    ).order_by(func.sum(InvoiceItem.quantity).desc()
    ).limit(10).all()
    
    return {
        "year": year,
        "month": month,
        "total_sales": float(sales.total_sales or 0),
        "total_invoices": sales.total_invoices or 0,
        "by_currency": {row[0]: float(row[1]) for row in currency_sales if row[0]},
        "daily_breakdown": [
            {
                "date": str(row.date),
                "total": float(row.total),
                "count": row.count
            }
            for row in daily_breakdown
        ],
        "top_products": [
            {
                "id": p.id,
                "name": p.name,
                "sku": p.sku,
                "total_quantity": float(p.total_quantity),
                "total_revenue": float(p.total_revenue)
            }
            for p in top_products
        ]
    }


@router.get("/overview")
def get_overview(
    db: Session = Depends(get_db),
    current_user = Depends(require_admin)
):
    today = datetime.now().date()
    week_start = today - timedelta(days=today.weekday())
    month_start = today.replace(day=1)
    
    # Today's sales
    today_sales = db.query(
        func.coalesce(func.sum(Invoice.total), 0)
    ).filter(
        func.date(Invoice.created_at) == today,
        Invoice.status == "completed"
    ).scalar() or 0
    
    # This week's sales
    week_sales = db.query(
        func.coalesce(func.sum(Invoice.total), 0)
    ).filter(
        Invoice.created_at >= week_start,
        Invoice.status == "completed"
    ).scalar() or 0
    
    # This month's sales
    month_sales = db.query(
        func.coalesce(func.sum(Invoice.total), 0)
    ).filter(
        Invoice.created_at >= month_start,
        Invoice.status == "completed"
    ).scalar() or 0
    
    # Total invoices
    total_invoices = db.query(Invoice).filter(Invoice.status == "completed").count()
    
    # Total products
    total_products = db.query(Product).count()
    
    # Low stock products (less than 10)
    low_stock = db.query(Product).join(Stock).group_by(Product.id).having(
        func.sum(Stock.quantity) < 10
    ).count()
    
    # Total stock value
    total_stock_value = db.query(
        func.coalesce(func.sum(Stock.quantity * Product.retail_price), 0)
    ).join(Product).scalar() or 0
    
    # Recent invoices
    recent_invoices = db.query(Invoice).filter(
        Invoice.status == "completed"
    ).order_by(Invoice.created_at.desc()).limit(5).all()
    
    return {
        "today_sales": float(today_sales),
        "week_sales": float(week_sales),
        "month_sales": float(month_sales),
        "total_invoices": total_invoices,
        "total_products": total_products,
        "low_stock_count": low_stock,
        "total_stock_value": float(total_stock_value),
        "recent_invoices": [
            {
                "id": inv.id,
                "invoice_number": inv.invoice_number,
                "customer_name": inv.customer_name,
                "total": float(inv.total),
                "created_at": inv.created_at.isoformat() if inv.created_at else None
            }
            for inv in recent_invoices
        ]
    }
