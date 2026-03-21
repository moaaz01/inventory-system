from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime


class CategoryBase(BaseModel):
    name: str
    description: Optional[str] = None
    parent_id: Optional[int] = None


class CategoryCreate(CategoryBase):
    pass


class CategoryUpdate(BaseModel):
    name: Optional[str] = None
    description: Optional[str] = None
    parent_id: Optional[int] = None


class CategoryResponse(CategoryBase):
    id: int
    created_at: datetime
    children: List["CategoryResponse"] = []

    model_config = {"from_attributes": True}


CategoryResponse.model_rebuild()
