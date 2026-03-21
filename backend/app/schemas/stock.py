from pydantic import BaseModel
from typing import Optional
from datetime import datetime


class StockResponse(BaseModel):
    id: int
    product_id: int
    product_name: str
    warehouse_id: int
    warehouse_name: str
    quantity: int
    min_stock_level: int
    is_low_stock: bool
    updated_at: datetime

    model_config = {"from_attributes": True}


class StockAdjust(BaseModel):
    quantity: int
    notes: Optional[str] = None
