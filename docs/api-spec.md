# Inventory Management API - OpenAPI Spec

Base URL: `http://localhost:8000`
Auth: JWT Bearer token

## Authentication

### POST /api/auth/register
Register new user.
```json
Request: { "username": "string", "email": "string", "password": "string" }
Response: { "id": 1, "username": "string", "email": "string", "role": "user" }
```

### POST /api/auth/login
Login and get JWT token.
```json
Request: { "username": "string", "password": "string" }
Response: { "access_token": "string", "token_type": "bearer" }
```

### GET /api/auth/me
Get current user info. Auth required.

---

## Products

### GET /api/products
List all products. Auth required.
```
Query: ?search=&category_id=&page=&size=
Response: { "items": [...], "total": N, "page": 1, "size": 20 }
```

### GET /api/products/{id}
Get product details with stock info per warehouse.

### POST /api/products
Create product. Admin only.
```json
Request: { "sku": "string", "name": "string", "category_id": 1, "unit_id": 1, "min_stock_level": 10 }
Response: { "id": 1, ... }
```

### PUT /api/products/{id}
Update product. Admin only.

### DELETE /api/products/{id}
Delete product. Admin only.

---

## Categories

### GET /api/categories
List all categories (tree structure).

### POST /api/categories
Create category. Admin only.

### PUT /api/categories/{id}

### DELETE /api/categories/{id}

---

## Units

### GET /api/units
List all units.

### POST /api/units
Create unit. Admin only.

---

## Warehouses

### GET /api/warehouses
List all warehouses.

### GET /api/warehouses/{id}
Get warehouse with products and stock.

### POST /api/warehouses
Create warehouse. Admin only.

### PUT /api/warehouses/{id}

### DELETE /api/warehouses/{id}

---

## Stock

### GET /api/stock
List stock levels across all warehouses.
```
Query: ?warehouse_id=&product_id=&low_stock=true
```

### PUT /api/stock/{product_id}/{warehouse_id}
Manual stock adjustment. Admin only.

---

## Stock Movements

### GET /api/movements
List all movements with filters.
```
Query: ?type=&product_id=&warehouse_id=&from_date=&to_date=&page=&size=
```

### POST /api/movements/receipt
Stock receipt (incoming).
```json
Request: { "product_id": 1, "warehouse_id": 1, "quantity": 100, "reference_number": "INV-001", "notes": "" }
```

### POST /api/movements/issue
Stock issue (outgoing).
```json
Request: { "product_id": 1, "warehouse_id": 1, "quantity": 5, "reference_number": "SO-001" }
```

### POST /api/movements/transfer
Transfer between warehouses.
```json
Request: { "product_id": 1, "from_warehouse_id": 1, "to_warehouse_id": 2, "quantity": 10 }
```

---

## Reports

### GET /api/reports/inventory
Current inventory report with stock levels.

### GET /api/reports/low-stock
Products below min_stock_level.

### GET /api/reports/movements
Movement history report.
```
Query: ?from_date=&to_date=&movement_type=
```

---

## Dashboard Stats (Admin)

### GET /api/dashboard/stats
```
Response: {
  "total_products": N,
  "total_warehouses": N,
  "total_stock_value": N,
  "low_stock_count": N,
  "recent_movements": N
}
```
