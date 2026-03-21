from sqlalchemy import Column, Integer, String, Text, ForeignKey, DateTime
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.database import Base


class StockMovement(Base):
    __tablename__ = "stock_movements"

    id = Column(Integer, primary_key=True, index=True)
    product_id = Column(Integer, ForeignKey("products.id"), nullable=False)
    warehouse_id = Column(Integer, ForeignKey("warehouses.id"), nullable=False)
    to_warehouse_id = Column(Integer, ForeignKey("warehouses.id"), nullable=True)
    movement_type = Column(String(20), nullable=False)  # receipt, issue, transfer, adjustment
    quantity = Column(Integer, nullable=False)
    reference_number = Column(String(100))
    notes = Column(Text)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    created_at = Column(DateTime, server_default=func.now(), index=True)

    product = relationship("Product", back_populates="movements")
    warehouse = relationship("Warehouse", foreign_keys=[warehouse_id], back_populates="movements")
    to_warehouse = relationship("Warehouse", foreign_keys=[to_warehouse_id])
    user = relationship("User", back_populates="movements")
