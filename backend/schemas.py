from typing import List, Literal, Optional

from common import Status

from datetime import datetime

from pydantic import BaseModel


class ItemSynonymBase(BaseModel):
    synonym: str


class ItemSynonymCreate(ItemSynonymBase):
    pass


class ItemSynonym(ItemSynonymBase):
    id: int
    item_id: int

    class Config:
        orm_mode = True


class ItemBase(BaseModel):
    name: str
    price: int


class ItemCreate(ItemBase):
    pass


class Item(ItemBase):
    id: int
    category_id: int
    synonyms: List[ItemSynonym] = []

    class Config:
        orm_mode = True


class CategorySynonymBase(BaseModel):
    synonym: str


class CategorySynonymCreate(CategorySynonymBase):
    pass


class CategorySynonym(CategorySynonymBase):
    id: int
    category_id: int

    class Config:
        orm_mode = True


class CategoryBase(BaseModel):
    name: str


class CategoryCreate(CategoryBase):
    pass


class Category(CategoryBase):
    id: int
    items: List[Item] = []
    synonyms: List[CategorySynonym] = []

    class Config:
        orm_mode = True


class OrderItemBase(BaseModel):
    item: Optional[Item]
    quantity: int


class OrderItemCreate(OrderItemBase):
    pass


class OrderItem(OrderItemBase):
    id: int
    order_id: int

    class Config:
        orm_mode = True


class OrderItemBaseAdd(BaseModel):
    item_id: int
    quantity: int


class OrderItemBaseNameAdd(BaseModel):
    name: str
    quantity: int


class OrderBase(BaseModel):
    status: Status = Status.submitted


class OrderCreate(OrderBase):
    pass


class Order(OrderBase):
    id: Optional[int]
    date: Optional[datetime]
    items: List[OrderItem] = []

    class Config:
        orm_mode = True


class SynonymCheckerBase(BaseModel):
    synonym: str


class SynonymCheckerCreate(SynonymCheckerBase):
    pass
