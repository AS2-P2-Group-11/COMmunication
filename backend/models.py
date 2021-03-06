from sqlalchemy import Boolean, Column, ForeignKey, Integer, String, DateTime
from sqlalchemy.orm import relationship

import datetime

from database import Base


class Category(Base):
    __tablename__ = "categories"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(50), unique=True, index=True)
    items = relationship("Item", back_populates="category")
    synonyms = relationship("CategorySynonym", back_populates="category")


class CategorySynonym(Base):
    __tablename__ = "category_synonyms"

    id = Column(Integer, primary_key=True, index=True)
    synonym = Column(String(50), unique=True, index=True)
    category_id = Column(Integer, ForeignKey("categories.id"))
    category = relationship("Category", back_populates="synonyms", uselist=False)


class Item(Base):
    __tablename__ = "items"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(50), index=True)
    price = Column(Integer)
    category_id = Column(Integer, ForeignKey("categories.id"))
    category = relationship("Category", back_populates="items")
    order_items = relationship("OrderItem", back_populates="item")
    synonyms = relationship("ItemSynonym", back_populates="item")


class ItemSynonym(Base):
    __tablename__ = "item_synonyms"

    id = Column(Integer, primary_key=True, index=True)
    synonym = Column(String(50), unique=True, index=True)
    item_id = Column(Integer, ForeignKey("items.id"))
    item = relationship("Item", back_populates="synonyms", uselist=False)


class Order(Base):
    __tablename__ = "orders"

    id = Column(Integer, primary_key=True, index=True)
    date = Column(DateTime, default=datetime.datetime)
    status = Column(String(50), index=True)
    items = relationship("OrderItem", back_populates="order")


class OrderItem(Base):
    __tablename__ = "order_items"

    id = Column(Integer, primary_key=True, index=True)
    item_id = Column(Integer, ForeignKey("items.id"))
    item = relationship("Item", back_populates="order_items")
    quantity = Column(Integer)
    order_id = Column(Integer, ForeignKey("orders.id"))
    order = relationship("Order", back_populates="items", uselist=False)