from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from typing import Optional
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.unit import Unit
from app.api.deps import get_current_user, require_admin
from app.models.user import User

router = APIRouter(prefix="/api/units", tags=["units"])


class UnitCreate(BaseModel):
    name: str
    symbol: str


class UnitResponse(BaseModel):
    id: int
    name: str
    symbol: str
    model_config = {"from_attributes": True}


@router.get("", response_model=list[UnitResponse])
def list_units(db: Session = Depends(get_db), _: User = Depends(get_current_user)):
    return db.query(Unit).all()


@router.post("", response_model=UnitResponse, status_code=201)
def create_unit(data: UnitCreate, db: Session = Depends(get_db), _: User = Depends(require_admin)):
    unit = Unit(**data.model_dump())
    db.add(unit)
    db.commit()
    db.refresh(unit)
    return unit


@router.delete("/{id}", status_code=204)
def delete_unit(id: int, db: Session = Depends(get_db), _: User = Depends(require_admin)):
    unit = db.query(Unit).filter(Unit.id == id).first()
    if not unit:
        raise HTTPException(404, "Unit not found")
    db.delete(unit)
    db.commit()
