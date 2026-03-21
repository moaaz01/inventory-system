"""
Smoke tests for the Inventory Management System API.
Uses an in-memory SQLite database for isolation.
"""
import os
import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

# Set TESTING env before importing app
os.environ["TESTING"] = "1"

from app.database import Base, get_db
from app.main import app
from app.core.security import hash_password

SQLALCHEMY_TEST_URL = "sqlite:///./test.db"

engine_test = create_engine(SQLALCHEMY_TEST_URL, connect_args={"check_same_thread": False})
TestingSession = sessionmaker(autocommit=False, autoflush=False, bind=engine_test)


def override_get_db():
    db = TestingSession()
    try:
        yield db
    finally:
        db.close()


app.dependency_overrides[get_db] = override_get_db


@pytest.fixture(scope="session", autouse=True)
def setup_db():
    Base.metadata.create_all(bind=engine_test)
    yield
    Base.metadata.drop_all(bind=engine_test)
    if os.path.exists("test.db"):
        os.remove("test.db")


@pytest.fixture(scope="session")
def client():
    return TestClient(app)


def create_user_direct(username, email, password, role="user"):
    """Create user directly in DB, bypassing rate-limited register endpoint."""
    from app.models.user import User
    db = TestingSession()
    existing = db.query(User).filter(User.username == username).first()
    if not existing:
        user = User(
            username=username,
            email=email,
            password_hash=hash_password(password),
            role=role,
            is_active=True,
        )
        db.add(user)
        db.commit()
    db.close()


@pytest.fixture(scope="session")
def admin_token(client):
    create_user_direct("testadmin", "testadmin@test.com", "testpass123", "admin")
    resp = client.post("/api/auth/login", json={"username": "testadmin", "password": "testpass123"})
    assert resp.status_code == 200, f"Login failed: {resp.json()}"
    return resp.json()["access_token"]


@pytest.fixture(scope="session")
def user_token(client):
    create_user_direct("testuser", "testuser@test.com", "testpass123", "user")
    resp = client.post("/api/auth/login", json={"username": "testuser", "password": "testpass123"})
    assert resp.status_code == 200, f"Login failed: {resp.json()}"
    return resp.json()["access_token"]


def auth(token):
    return {"Authorization": f"Bearer {token}"}


# ---- Auth ----

def test_register(client):
    resp = client.post("/api/auth/register", json={
        "username": "newuser1",
        "email": "newuser1@test.com",
        "password": "password123",
    })
    assert resp.status_code == 201
    assert resp.json()["username"] == "newuser1"


def test_register_duplicate(client):
    create_user_direct("dupuser", "dup@test.com", "password123")
    resp = client.post("/api/auth/register", json={
        "username": "dupuser",
        "email": "dup2@test.com",
        "password": "password123",
    })
    assert resp.status_code == 400


def test_login(client):
    create_user_direct("loginuser", "login@test.com", "pass1234")
    resp = client.post("/api/auth/login", json={"username": "loginuser", "password": "pass1234"})
    assert resp.status_code == 200
    assert "access_token" in resp.json()


def test_login_wrong_password(client):
    create_user_direct("badpass_user", "badpass@test.com", "correctpass")
    resp = client.post("/api/auth/login", json={"username": "badpass_user", "password": "wrongpass"})
    assert resp.status_code == 401


def test_me(client, user_token):
    resp = client.get("/api/auth/me", headers=auth(user_token))
    assert resp.status_code == 200
    assert resp.json()["username"] == "testuser"


def test_me_no_auth(client):
    resp = client.get("/api/auth/me")
    assert resp.status_code == 403


# ---- Categories ----

def test_create_category(client, admin_token):
    resp = client.post("/api/categories", json={"name": "Electronics"}, headers=auth(admin_token))
    assert resp.status_code == 201
    assert resp.json()["name"] == "Electronics"


def test_list_categories(client, user_token):
    resp = client.get("/api/categories", headers=auth(user_token))
    assert resp.status_code == 200
    assert isinstance(resp.json(), list)


def test_update_category(client, admin_token):
    resp = client.post("/api/categories", json={"name": "TempCat"}, headers=auth(admin_token))
    cat_id = resp.json()["id"]
    resp2 = client.put(f"/api/categories/{cat_id}", json={"name": "UpdatedCat"}, headers=auth(admin_token))
    assert resp2.status_code == 200
    assert resp2.json()["name"] == "UpdatedCat"


def test_delete_category(client, admin_token):
    resp = client.post("/api/categories", json={"name": "DeleteMe"}, headers=auth(admin_token))
    cat_id = resp.json()["id"]
    resp2 = client.delete(f"/api/categories/{cat_id}", headers=auth(admin_token))
    assert resp2.status_code == 204


# ---- Units ----

def test_create_unit(client, admin_token):
    resp = client.post("/api/units", json={"name": "piece", "symbol": "pc"}, headers=auth(admin_token))
    assert resp.status_code == 201


def test_list_units(client, user_token):
    resp = client.get("/api/units", headers=auth(user_token))
    assert resp.status_code == 200


# ---- Warehouses ----

def test_create_warehouse(client, admin_token):
    resp = client.post("/api/warehouses", json={"name": "Main WH", "location": "Zone A"}, headers=auth(admin_token))
    assert resp.status_code == 201
    assert resp.json()["name"] == "Main WH"


def test_list_warehouses(client, user_token):
    resp = client.get("/api/warehouses", headers=auth(user_token))
    assert resp.status_code == 200


# ---- Products ----

@pytest.fixture(scope="session")
def product_and_warehouse(client, admin_token):
    cat_resp = client.post("/api/categories", json={"name": "TestCat"}, headers=auth(admin_token))
    unit_resp = client.post("/api/units", json={"name": "box", "symbol": "bx"}, headers=auth(admin_token))
    wh_resp = client.post("/api/warehouses", json={"name": "TestWH"}, headers=auth(admin_token))
    prod_resp = client.post("/api/products", json={
        "sku": "TEST-001",
        "name": "Test Product",
        "category_id": cat_resp.json()["id"],
        "unit_id": unit_resp.json()["id"],
        "min_stock_level": 5,
    }, headers=auth(admin_token))
    assert prod_resp.status_code == 201, prod_resp.json()
    return prod_resp.json()["id"], wh_resp.json()["id"]


def test_list_products(client, user_token):
    resp = client.get("/api/products", headers=auth(user_token))
    assert resp.status_code == 200
    assert "items" in resp.json()


def test_get_product(client, user_token, product_and_warehouse):
    prod_id, _ = product_and_warehouse
    resp = client.get(f"/api/products/{prod_id}", headers=auth(user_token))
    assert resp.status_code == 200


def test_product_not_found(client, user_token):
    resp = client.get("/api/products/99999", headers=auth(user_token))
    assert resp.status_code == 404


def test_update_product(client, admin_token, product_and_warehouse):
    prod_id, _ = product_and_warehouse
    resp = client.put(f"/api/products/{prod_id}", json={"name": "Updated Product"}, headers=auth(admin_token))
    assert resp.status_code == 200
    assert resp.json()["name"] == "Updated Product"


# ---- Stock Movements ----

def test_receipt(client, admin_token, product_and_warehouse):
    prod_id, wh_id = product_and_warehouse
    resp = client.post("/api/movements/receipt", json={
        "product_id": prod_id,
        "warehouse_id": wh_id,
        "quantity": 100,
        "reference_number": "INV-001",
    }, headers=auth(admin_token))
    assert resp.status_code == 201
    assert resp.json()["movement_type"] == "receipt"


def test_issue(client, admin_token, product_and_warehouse):
    prod_id, wh_id = product_and_warehouse
    resp = client.post("/api/movements/issue", json={
        "product_id": prod_id,
        "warehouse_id": wh_id,
        "quantity": 10,
    }, headers=auth(admin_token))
    assert resp.status_code == 201


def test_issue_insufficient(client, admin_token, product_and_warehouse):
    prod_id, wh_id = product_and_warehouse
    resp = client.post("/api/movements/issue", json={
        "product_id": prod_id,
        "warehouse_id": wh_id,
        "quantity": 999999,
    }, headers=auth(admin_token))
    assert resp.status_code == 400


def test_transfer(client, admin_token, product_and_warehouse):
    prod_id, wh_id = product_and_warehouse
    wh2_resp = client.post("/api/warehouses", json={"name": "WH2"}, headers=auth(admin_token))
    wh2_id = wh2_resp.json()["id"]
    resp = client.post("/api/movements/transfer", json={
        "product_id": prod_id,
        "from_warehouse_id": wh_id,
        "to_warehouse_id": wh2_id,
        "quantity": 5,
    }, headers=auth(admin_token))
    assert resp.status_code == 201


def test_list_movements(client, user_token):
    resp = client.get("/api/movements", headers=auth(user_token))
    assert resp.status_code == 200
    assert "items" in resp.json()


# ---- Stock ----

def test_list_stock(client, user_token):
    resp = client.get("/api/stock", headers=auth(user_token))
    assert resp.status_code == 200


def test_adjust_stock(client, admin_token, product_and_warehouse):
    prod_id, wh_id = product_and_warehouse
    resp = client.put(f"/api/stock/{prod_id}/{wh_id}", json={"quantity": 50}, headers=auth(admin_token))
    assert resp.status_code == 200
    assert resp.json()["quantity"] == 50


# ---- Reports ----

def test_inventory_report(client, user_token):
    resp = client.get("/api/reports/inventory", headers=auth(user_token))
    assert resp.status_code == 200
    assert isinstance(resp.json(), list)


def test_low_stock_report(client, user_token):
    resp = client.get("/api/reports/low-stock", headers=auth(user_token))
    assert resp.status_code == 200


def test_movements_report(client, user_token):
    resp = client.get("/api/reports/movements", headers=auth(user_token))
    assert resp.status_code == 200


def test_dashboard_stats(client, admin_token):
    resp = client.get("/api/dashboard/stats", headers=auth(admin_token))
    assert resp.status_code == 200
    data = resp.json()
    assert "total_products" in data
    assert "total_warehouses" in data
    assert "low_stock_count" in data


def test_health(client):
    resp = client.get("/health")
    assert resp.status_code == 200
