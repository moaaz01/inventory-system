from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from app.database import get_db
from app.models.user import User
from app.schemas.user import UserCreate, UserUpdate, UserRoleUpdate, UserDetailResponse
from app.schemas.auth import UserResponse
from app.core.security import hash_password
from app.api.deps import require_admin

router = APIRouter(prefix="/api/users", tags=["users"])


@router.get("", response_model=List[UserDetailResponse])
def list_users(db: Session = Depends(get_db), _: User = Depends(require_admin)):
    return db.query(User).all()


@router.post("", response_model=UserDetailResponse, status_code=201)
def create_user(data: UserCreate, db: Session = Depends(get_db), _: User = Depends(require_admin)):
    if db.query(User).filter(User.username == data.username).first():
        raise HTTPException(400, "Username already taken")
    if db.query(User).filter(User.email == data.email).first():
        raise HTTPException(400, "Email already registered")
    if data.role not in ("admin", "user"):
        raise HTTPException(400, "Role must be 'admin' or 'user'")
    user = User(
        username=data.username,
        email=data.email,
        password_hash=hash_password(data.password),
        role=data.role,
    )
    db.add(user)
    db.commit()
    db.refresh(user)
    return user


@router.put("/{id}", response_model=UserDetailResponse)
def update_user(id: int, data: UserUpdate, db: Session = Depends(get_db), _: User = Depends(require_admin)):
    user = db.query(User).filter(User.id == id).first()
    if not user:
        raise HTTPException(404, "User not found")
    update_data = data.model_dump(exclude_unset=True)
    if "role" in update_data and update_data["role"] not in ("admin", "user"):
        raise HTTPException(400, "Role must be 'admin' or 'user'")
    for k, v in update_data.items():
        setattr(user, k, v)
    db.commit()
    db.refresh(user)
    return user


@router.delete("/{id}", status_code=204)
def delete_user(id: int, db: Session = Depends(get_db), admin: User = Depends(require_admin)):
    user = db.query(User).filter(User.id == id).first()
    if not user:
        raise HTTPException(404, "User not found")
    if user.id == admin.id:
        raise HTTPException(400, "Cannot delete yourself")
    db.delete(user)
    db.commit()


@router.put("/{id}/role", response_model=UserDetailResponse)
def update_user_role(id: int, data: UserRoleUpdate, db: Session = Depends(get_db), _: User = Depends(require_admin)):
    user = db.query(User).filter(User.id == id).first()
    if not user:
        raise HTTPException(404, "User not found")
    if data.role not in ("admin", "user"):
        raise HTTPException(400, "Role must be 'admin' or 'user'")
    user.role = data.role
    db.commit()
    db.refresh(user)
    return user
