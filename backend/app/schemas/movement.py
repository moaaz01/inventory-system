from pydantic import BaseModel, field_validator
from typing import Optional
from datetime import datetime


class MovementResponse(BaseModel):
    id: int
    product_id: int
    product_name: str
    warehouse_id: int
    warehouse_name: str
    to_warehouse_id: Optional[int] = None
    movement_type: str
    quantity: int
    reference_number: Optional[str] = None
    notes: Optional[str] = None
    user_id: int
    created_at: datetime

    model_config = {"from_attributes": True}


class ReceiptRequest(BaseModel):
    product_id: int
    warehouse_id: int
    quantity: int
    reference_number: Optional[str] = None
    notes: Optional[str] = None

    @field_validator("quantity")
    @classmethod
    def quantity_positive(cls, v: int) -> int:
        if v <= 0:
            raise ValueError("Quantity must be positive")
        return v


class IssueRequest(BaseModel):
    product_id: int
    warehouse_id: int
    quantity: int
    reference_number: Optional[str] = None
    notes: Optional[str] = None

    @field_validator("quantity")
    @classmethod
    def quantity_positive(cls, v: int) -> int:
        if v <= 0:
            raise ValueError("Quantity must be positive")
        return v


class TransferRequest(BaseModel):
    product_id: int
    from_warehouse_id: int
    to_warehouse_id: int
    quantity: int
    notes: Optional[str] = None

    @field_validator("quantity")
    @classmethod
    def quantity_positive(cls, v: int) -> int:
        if v <= 0:
            raise ValueError("Quantity must be positive")
        return v


class MovementListResponse(BaseModel):
    items: list[MovementResponse]
    total: int
    page: int
    size: int
