from sqlalchemy import Column, Integer, String
from sqlalchemy.orm import relationship
from app.database import Base


class Unit(Base):
    __tablename__ = "units"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(50), nullable=False)
    symbol = Column(String(10), nullable=False)

    products = relationship("Product", back_populates="unit")
