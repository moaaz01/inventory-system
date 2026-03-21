from sqlalchemy import Column, Integer, String, ForeignKey, DateTime
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
from app.database import Base


class Warehouse(Base):
    __tablename__ = "warehouses"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100), nullable=False)
    location = Column(String(255))
    manager_id = Column(Integer, ForeignKey("users.id"), nullable=True)
    created_at = Column(DateTime, server_default=func.now())

    manager = relationship("User", back_populates="warehouses")
    stock = relationship("Stock", back_populates="warehouse")
    movements = relationship("StockMovement", foreign_keys="StockMovement.warehouse_id", back_populates="warehouse")
