from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from sqlalchemy import or_
from typing import Optional
from app.database import get_db
from app.models.product import Product
from app.schemas.product import ProductCreate, ProductUpdate, ProductResponse, ProductListResponse, StockInfo
from app.api.deps import get_current_user, require_admin
from app.models.user import User

router = APIRouter(prefix="/api/products", tags=["products"])


def enrich_product(p: Product) -> ProductResponse:
    stock_info = [
        StockInfo(warehouse_id=s.warehouse_id, warehouse_name=s.warehouse.name, quantity=s.quantity)
        for s in p.stock
    ]
    return ProductResponse(
        id=p.id,
        sku=p.sku,
        name=p.name,
        description=p.description,
        category_id=p.category_id,
        unit_id=p.unit_id,
        min_stock_level=p.min_stock_level,
        barcode=p.barcode,
        image_url=p.image_url,
        created_at=p.created_at,
        updated_at=p.updated_at,
        stock=stock_info,
        category_name=p.category.name if p.category else None,
        unit_name=p.unit.name if p.unit else None,
        retail_price=float(p.retail_price) if p.retail_price is not None else None,
        wholesale_price=float(p.wholesale_price) if p.wholesale_price is not None else None,
        currency=p.currency or "USD",
    )


@router.get("/search", response_model=list[ProductResponse])
def search_products(
    q: str = Query(..., min_length=1),
    db: Session = Depends(get_db),
    _: User = Depends(get_current_user),
):
    """Search products by name or SKU - for autocomplete dropdowns."""
    items = db.query(Product).filter(
        or_(Product.name.ilike(f"%{q}%"), Product.sku.ilike(f"%{q}%"))
    ).limit(20).all()
    return [enrich_product(p) for p in items]


@router.get("/all", response_model=list[ProductResponse])
def list_all_products(
    db: Session = Depends(get_db),
    _: User = Depends(get_current_user),
):
    """Return all products without pagination - for dropdowns."""
    items = db.query(Product).all()
    return [enrich_product(p) for p in items]


@router.get("", response_model=ProductListResponse)
def list_products(
    search: Optional[str] = None,
    category_id: Optional[int] = None,
    page: int = Query(1, ge=1),
    size: int = Query(20, ge=1, le=100),
    db: Session = Depends(get_db),
    _: User = Depends(get_current_user),
):
    q = db.query(Product)
    if search:
        q = q.filter(Product.name.ilike(f"%{search}%") | Product.sku.ilike(f"%{search}%"))
    if category_id:
        q = q.filter(Product.category_id == category_id)
    total = q.count()
    items = q.offset((page - 1) * size).limit(size).all()
    return ProductListResponse(items=[enrich_product(p) for p in items], total=total, page=page, size=size)


@router.get("/{id}", response_model=ProductResponse)
def get_product(id: int, db: Session = Depends(get_db), _: User = Depends(get_current_user)):
    p = db.query(Product).filter(Product.id == id).first()
    if not p:
        raise HTTPException(404, "Product not found")
    return enrich_product(p)


@router.post("", response_model=ProductResponse, status_code=201)
def create_product(data: ProductCreate, db: Session = Depends(get_db), _: User = Depends(require_admin)):
    if db.query(Product).filter(Product.sku == data.sku).first():
        raise HTTPException(400, "SKU already exists")
    p = Product(**data.model_dump())
    db.add(p)
    db.commit()
    db.refresh(p)
    return enrich_product(p)


@router.put("/{id}", response_model=ProductResponse)
def update_product(id: int, data: ProductUpdate, db: Session = Depends(get_db), _: User = Depends(require_admin)):
    p = db.query(Product).filter(Product.id == id).first()
    if not p:
        raise HTTPException(404, "Product not found")
    for k, v in data.model_dump(exclude_unset=True).items():
        setattr(p, k, v)
    db.commit()
    db.refresh(p)
    return enrich_product(p)


@router.delete("/{id}", status_code=204)
def delete_product(id: int, db: Session = Depends(get_db), _: User = Depends(require_admin)):
    p = db.query(Product).filter(Product.id == id).first()
    if not p:
        raise HTTPException(404, "Product not found")
    db.delete(p)
    db.commit()
