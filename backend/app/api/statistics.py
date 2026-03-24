from fastapi import APIRouter, Depends, Query, HTTPException
from sqlalchemy.orm import Session
from sqlalchemy import func
from datetime import datetime
from typing import Optional

from app.database import get_db
from app.api.deps import require_admin
from app.models.invoice import Invoice

router = APIRouter(prefix="/api/statistics", tags=["Statistics"])

@router.get("/sales/daily")
def get_daily_sales(
    date: str = Query(..., description="Date in YYYY-MM-DD format"),
    db: Session = Depends(get_db),
    current_user = Depends(require_admin)
):
    try:
        target_date = datetime.strptime(date, "%Y-%m-%d").date()
    except ValueError:
        raise HTTPException(status_code=400, detail="Invalid date format. Use YYYY-MM-DD")
    
    sales = db.query(
        func.sum(Invoice.total).label("total_sales"),
        func.count(Invoice.id).label("total_invoices")
    ).filter(
        func.date(Invoice.created_at) == target_date,
        Invoice.status == "completed"
    ).first()
    
    return {
        "date": date,
        "total_sales": sales.total_sales or 0,
        "total_invoices": sales.total_invoices or 0
    }

@router.get("/sales/monthly")
def get_monthly_sales(
    year: int = Query(...),
    month: int = Query(..., ge=1, le=12),
    db: Session = Depends(get_db),
    current_user = Depends(require_admin)
):
    sales = db.query(
        func.sum(Invoice.total).label("total_sales"),
        func.count(Invoice.id).label("total_invoices")
    ).filter(
        func.extract("year", Invoice.created_at) == year,
        func.extract("month", Invoice.created_at) == month,
        Invoice.status == "completed"
    ).first()
    
    return {
        "year": year,
        "month": month,
        "total_sales": sales.total_sales or 0,
        "total_invoices": sales.total_invoices or 0
    }

@router.get("/overview")
def get_overview(
    db: Session = Depends(get_db),
    current_user = Depends(require_admin)
):
    total_sales = db.query(func.sum(Invoice.total)).filter(Invoice.status == "completed").scalar() or 0
    total_invoices = db.query(Invoice).filter(Invoice.status == "completed").count()
    
    return {
        "total_sales": total_sales,
        "total_invoices": total_invoices
    }
