from pydantic import BaseModel
from typing import Optional
from datetime import datetime


class WarehouseBase(BaseModel):
    name: str
    location: Optional[str] = None
    manager_id: Optional[int] = None


class WarehouseCreate(WarehouseBase):
    pass


class WarehouseUpdate(BaseModel):
    name: Optional[str] = None
    location: Optional[str] = None
    manager_id: Optional[int] = None


class WarehouseResponse(WarehouseBase):
    id: int
    created_at: datetime

    model_config = {"from_attributes": True}
