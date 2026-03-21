from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.warehouse import Warehouse
from app.schemas.warehouse import WarehouseCreate, WarehouseUpdate, WarehouseResponse
from app.api.deps import get_current_user, require_admin
from app.models.user import User

router = APIRouter(prefix="/api/warehouses", tags=["warehouses"])


@router.get("", response_model=list[WarehouseResponse])
def list_warehouses(db: Session = Depends(get_db), _: User = Depends(get_current_user)):
    return db.query(Warehouse).all()


@router.get("/{id}", response_model=WarehouseResponse)
def get_warehouse(id: int, db: Session = Depends(get_db), _: User = Depends(get_current_user)):
    wh = db.query(Warehouse).filter(Warehouse.id == id).first()
    if not wh:
        raise HTTPException(404, "Warehouse not found")
    return wh


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
    db.delete(wh)
    db.commit()
