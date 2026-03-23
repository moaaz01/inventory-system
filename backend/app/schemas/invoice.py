from pydantic import BaseModel
from datetime import datetime
from typing import Optional, List

class InvoiceItemCreate(BaseModel):
    product_id: int
    quantity: float
    unit_price: float

class InvoiceItemResponse(BaseModel):
    id: int
    product_id: int
    product_name: str
    product_sku: str
    quantity: float
    unit_price: float
    total_price: float

    class Config:
        from_attributes = True

class InvoiceCreate(BaseModel):
    customer_name: Optional[str] = None
    discount: float = 0
    items: List[InvoiceItemCreate]

class InvoiceResponse(BaseModel):
    id: int
    invoice_number: str
    customer_name: Optional[str]
    subtotal: float
    discount: float
    total: float
    status: str
    created_at: datetime
    items: List[InvoiceItemResponse]

    class Config:
        from_attributes = True
