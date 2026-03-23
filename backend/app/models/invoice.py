from sqlalchemy import Column, Integer, String, Float, DateTime, ForeignKey
from sqlalchemy.orm import relationship
from app.database import Base
from datetime import datetime

class Invoice(Base):
    __tablename__ = "invoices"
    
    id = Column(Integer, primary_key=True, index=True)
    invoice_number = Column(String(50), unique=True, index=True)
    customer_name = Column(String(200), nullable=True)
    subtotal = Column(Float, default=0)
    discount = Column(Float, default=0)
    total = Column(Float, default=0)
    status = Column(String(20), default="completed")  # completed, cancelled
    created_at = Column(DateTime, default=datetime.utcnow)
    created_by = Column(Integer, ForeignKey("users.id"))
    
    items = relationship("InvoiceItem", back_populates="invoice", cascade="all, delete-orphan")

class InvoiceItem(Base):
    __tablename__ = "invoice_items"
    
    id = Column(Integer, primary_key=True, index=True)
    invoice_id = Column(Integer, ForeignKey("invoices.id"))
    product_id = Column(Integer, ForeignKey("products.id"), nullable=True)
    product_name = Column(String(200))  # snapshot
    product_sku = Column(String(50))
    quantity = Column(Float)
    unit_price = Column(Float)
    total_price = Column(Float)
    
    invoice = relationship("Invoice", back_populates="items")
