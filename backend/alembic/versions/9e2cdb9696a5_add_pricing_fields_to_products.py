"""add_pricing_fields_to_products

Revision ID: 9e2cdb9696a5
Revises: 
Create Date: 2026-03-22

"""
from alembic import op
import sqlalchemy as sa


revision = '9e2cdb9696a5'
down_revision = None
branch_labels = None
depends_on = None


def upgrade():
    op.add_column('products', sa.Column('retail_price', sa.Numeric(10, 2), nullable=True))
    op.add_column('products', sa.Column('wholesale_price', sa.Numeric(10, 2), nullable=True))
    op.add_column('products', sa.Column('currency', sa.String(3), nullable=True, server_default='USD'))


def downgrade():
    op.drop_column('products', 'currency')
    op.drop_column('products', 'wholesale_price')
    op.drop_column('products', 'retail_price')
