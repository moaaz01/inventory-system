#!/usr/bin/env python3
"""Seed initial data into the database."""
import sys
import os
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.database import SessionLocal, engine
from app.models import *
from app.database import Base
from app.core.security import hash_password

def seed():
    Base.metadata.create_all(bind=engine)
    db = SessionLocal()
    try:
        # Admin user
        if not db.query(User).filter(User.username == "admin").first():
            admin = User(
                username="admin",
                email="admin@inventory.local",
                password_hash=hash_password("admin123"),
                role="admin",
            )
            db.add(admin)
            db.flush()
        else:
            admin = db.query(User).filter(User.username == "admin").first()

        # Units
        units_data = [("piece", "pc"), ("kilogram", "kg"), ("box", "box")]
        units = []
        for name, symbol in units_data:
            u = db.query(Unit).filter(Unit.symbol == symbol).first()
            if not u:
                u = Unit(name=name, symbol=symbol)
                db.add(u)
                db.flush()
            units.append(u)

        # Categories
        cats_data = ["Electronics", "Clothing", "Food & Beverage", "Tools", "Office Supplies"]
        categories = []
        for name in cats_data:
            c = db.query(Category).filter(Category.name == name).first()
            if not c:
                c = Category(name=name)
                db.add(c)
                db.flush()
            categories.append(c)

        # Warehouses
        wh_data = [("Main Warehouse", "Building A, Zone 1"), ("Secondary Warehouse", "Building B, Zone 2")]
        warehouses = []
        for name, location in wh_data:
            wh = db.query(Warehouse).filter(Warehouse.name == name).first()
            if not wh:
                wh = Warehouse(name=name, location=location, manager_id=admin.id)
                db.add(wh)
                db.flush()
            warehouses.append(wh)

        # Products
        products_data = [
            ("ELEC-001", "Laptop Pro 15", categories[0], units[0], 5),
            ("ELEC-002", "Wireless Mouse", categories[0], units[0], 20),
            ("ELEC-003", "USB-C Hub", categories[0], units[0], 15),
            ("CLOTH-001", "Cotton T-Shirt L", categories[1], units[0], 50),
            ("CLOTH-002", "Denim Jeans 32", categories[1], units[0], 30),
            ("FOOD-001", "Coffee Beans 1kg", categories[2], units[1], 10),
            ("FOOD-002", "Green Tea Box", categories[2], units[2], 25),
            ("TOOL-001", "Screwdriver Set", categories[3], units[2], 8),
            ("TOOL-002", "Electric Drill", categories[3], units[0], 5),
            ("OFFC-001", "A4 Paper Ream", categories[4], units[2], 40),
        ]
        products = []
        for sku, name, cat, unit, min_stock in products_data:
            p = db.query(Product).filter(Product.sku == sku).first()
            if not p:
                p = Product(
                    sku=sku,
                    name=name,
                    category_id=cat.id,
                    unit_id=unit.id,
                    min_stock_level=min_stock,
                )
                db.add(p)
                db.flush()
            products.append(p)

        # Initial stock
        stock_levels = [100, 200, 150, 300, 250, 50, 80, 40, 20, 500]
        for i, (product, qty) in enumerate(zip(products, stock_levels)):
            wh = warehouses[i % 2]
            s = db.query(Stock).filter(Stock.product_id == product.id, Stock.warehouse_id == wh.id).first()
            if not s:
                s = Stock(product_id=product.id, warehouse_id=wh.id, quantity=qty)
                db.add(s)
                # movement
                m = StockMovement(
                    product_id=product.id,
                    warehouse_id=wh.id,
                    movement_type="receipt",
                    quantity=qty,
                    reference_number=f"SEED-{i+1:03d}",
                    notes="Initial stock",
                    user_id=admin.id,
                )
                db.add(m)

        db.commit()
        print("✅ Seed data created successfully!")
    except Exception as e:
        db.rollback()
        print(f"❌ Seed failed: {e}")
        raise
    finally:
        db.close()


if __name__ == "__main__":
    seed()
