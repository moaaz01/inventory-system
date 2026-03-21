from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.category import Category
from app.schemas.category import CategoryCreate, CategoryUpdate, CategoryResponse
from app.api.deps import get_current_user, require_admin
from app.models.user import User

router = APIRouter(prefix="/api/categories", tags=["categories"])


def build_tree(categories: list, parent_id=None):
    result = []
    for cat in categories:
        if cat.parent_id == parent_id:
            children = build_tree(categories, cat.id)
            item = CategoryResponse.model_validate(cat)
            item.children = children
            result.append(item)
    return result


@router.get("", response_model=list[CategoryResponse])
def list_categories(db: Session = Depends(get_db), _: User = Depends(get_current_user)):
    cats = db.query(Category).all()
    return build_tree(cats)


@router.post("", response_model=CategoryResponse, status_code=201)
def create_category(data: CategoryCreate, db: Session = Depends(get_db), _: User = Depends(require_admin)):
    cat = Category(**data.model_dump())
    db.add(cat)
    db.commit()
    db.refresh(cat)
    return cat


@router.put("/{id}", response_model=CategoryResponse)
def update_category(id: int, data: CategoryUpdate, db: Session = Depends(get_db), _: User = Depends(require_admin)):
    cat = db.query(Category).filter(Category.id == id).first()
    if not cat:
        raise HTTPException(404, "Category not found")
    for k, v in data.model_dump(exclude_unset=True).items():
        setattr(cat, k, v)
    db.commit()
    db.refresh(cat)
    return cat


@router.delete("/{id}", status_code=204)
def delete_category(id: int, db: Session = Depends(get_db), _: User = Depends(require_admin)):
    cat = db.query(Category).filter(Category.id == id).first()
    if not cat:
        raise HTTPException(404, "Category not found")
    db.delete(cat)
    db.commit()
