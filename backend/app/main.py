from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded
from app.core.config import settings

limiter = Limiter(key_func=get_remote_address)

app = FastAPI(
    title="نظام إدارة المخزون - Inventory Management System",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
)

app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Register routers
from app.api import auth, categories, units, warehouses, products, stock, movements, reports

app.include_router(auth.router)
app.include_router(categories.router)
app.include_router(units.router)
app.include_router(warehouses.router)
app.include_router(products.router)
app.include_router(stock.router)
app.include_router(movements.router)
app.include_router(reports.router)


@app.get("/health")
def health():
    return {"status": "ok"}


# Hide stack traces in production
if settings.ENVIRONMENT == "production":
    @app.exception_handler(Exception)
    async def generic_exception_handler(request, exc):
        return JSONResponse(status_code=500, content={"detail": "Internal server error"})
