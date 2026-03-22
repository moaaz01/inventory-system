from pydantic import BaseModel
from typing import Optional
from datetime import datetime


class ProductBase(BaseModel):
    sku: str
    name: str
    description: Optional[str] = None
    category_id: Optional[int] = None
    unit_id: Optional[int] = None
    min_stock_level: int = 10
    barcode: Optional[str] = None
    image_url: Optional[str] = None
    retail_price: Optional[float] = None
    wholesale_price: Optional[float] = None
    currency: str = "USD"


class ProductCreate(ProductBase):
    pass


class ProductUpdate(BaseModel):
    name: Optional[str] = None
    description: Optional[str] = None
    category_id: Optional[int] = None
    unit_id: Optional[int] = None
    min_stock_level: Optional[int] = None
    barcode: Optional[str] = None
    image_url: Optional[str] = None
    retail_price: Optional[float] = None
    wholesale_price: Optional[float] = None
    currency: Optional[str] = None


class StockInfo(BaseModel):
    warehouse_id: int
    warehouse_name: str
    quantity: int

    model_config = {"from_attributes": True}


class ProductResponse(ProductBase):
    id: int
    created_at: datetime
    updated_at: datetime
    stock: list[StockInfo] = []
    category_name: Optional[str] = None
    unit_name: Optional[str] = None

    model_config = {"from_attributes": True}


class ProductListResponse(BaseModel):
    items: list[ProductResponse]
    total: int
    page: int
    size: int
