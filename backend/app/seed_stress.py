#!/usr/bin/env python3
"""Seed 500 test products for stress testing."""
import random, sys, os
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from app.database import SessionLocal
from app.models.product import Product
from app.models.category import Category
from app.models.unit import Unit

CATEGORIES = ["إلكترونيات", "ملابس", "أغذية", "أدوات", "مستلزمات", "صحة", "رياضة", "كتب", "سيارات", "حديقة"]
UNITS = [("قطعة", "pc"), ("علبة", "box"), ("كيلو", "kg"), ("لتر", "L"), ("متر", "m"), ("دزينة", "doz")]
PRODUCT_PREFIXES = ["Super", "Pro", "Elite", "Basic", "Premium", "Standard", "Eco", "Max", "Ultra", "Mini"]
PRODUCT_NAMES_AR = ["لابتوب", "ماوس", "كيبورد", "شاحن", "سماعة", "كاميرا", "طابعة", "راوتر", "هاتف", "تابلت",
                     "قميص", "بنطلون", "حذاء", "جاكيت", "فستان", "عباية", "قفاز", "كوفية", "عقال", "حقيبة",
                     "رز", "قمح", "زيت", "سكر", "شاي", "قهوة", "حليب", "جبنة", "زبدة", "لحوم",
                     "مفك", "مطرقة", "منشار", "كماشة", "طاولة", "كرسي", "رف", "خزانة", "مرآة", "ساعة",
                     "صابون", "شامبو", "معجون", "كريم", "غسول", "باراسيتامول", "فيتامين", "ضماد", "مقياس ضغط", "ثيرمومتر",
                     "كرة", "حذاء رياضي", "مضرب", "سجادة يوغا", "دمبل", "حبل قفز", "سباحة", "دراجة", "كمالية", "قفاز box",
                     "رواية", "كتاب أطفال", "قاموس", "مجلة", "ورق", "قلم", "دفتر", "لوحة", "ألوان", "ممحاة",
                     "زيت محرك", "فلتر", "إطارات", "بوجيه", "بطارية", "مرآة سيارة", "مساحات", "شاحن سيارة", "كاميراdash", "ساعةdashboard",
                     "سماد", "بذور", "مجرفة", "ماءاة", "أصص", "تربة", "مبيد حشري", "خشب", "طلاء", "مسمار"]

db = SessionLocal()
print("Seeding categories...")
categories = []
for name in CATEGORIES:
    c = db.query(Category).filter(Category.name == name).first()
    if not c:
        c = Category(name=name, parent_id=None)
        db.add(c)
categories = db.query(Category).all()
if not categories:
    for name in CATEGORIES:
        db.add(Category(name=name, parent_id=None))
    db.commit()
    categories = db.query(Category).all()
print(f"  {len(categories)} categories")

print("Seeding units...")
units = []
for name, symbol in UNITS:
    u = db.query(Unit).filter(Unit.name == name).first()
    if not u:
        u = Unit(name=name, symbol=symbol)
        db.add(u)
units = db.query(Unit).all()
if not units:
    for name, symbol in UNITS:
        db.add(Unit(name=name, symbol=symbol))
    db.commit()
    units = db.query(Unit).all()
print(f"  {len(units)} units")

print("Seeding 500 products...")
created = 0
existing_skus = {p.sku for p in db.query(Product.sku).all()}
for i in range(1, 501):
    prefix = random.choice(PRODUCT_PREFIXES)
    name_ar = random.choice(PRODUCT_NAMES_AR)
    name = f"{prefix} {name_ar} {i}"
    sku = f"TEST-{i:04d}"
    if sku in existing_skus:
        continue
    cat = random.choice(categories) if categories else None
    unit = random.choice(units) if units else None
    p = Product(
        sku=sku,
        name=name,
        description=f"منتج تجريبي رقم {i}",
        category_id=cat.id if cat else None,
        unit_id=unit.id if unit else None,
        min_stock_level=random.randint(5, 50),
        barcode=f"{random.randint(100000000000, 999999999999)}"
    )
    db.add(p)
    created += 1
    if i % 50 == 0:
        db.commit()
        print(f"  {i}/500 committed...")

db.commit()
print(f"Done! Created {created} products. Total: {db.query(Product).count()}")
db.close()
