from sqlalchemy.orm import Session

from datetime import datetime

import models, schemas, common


def get_category_by_id(db: Session, category_id: int):
    return db.query(models.Category).filter(models.Category.id == category_id).first()


def get_category_by_name(db: Session, category_name: str):
    return db.query(models.Category).filter(models.Category.name == category_name).first()


def delete_category_by_id(db: Session, category_id: int):
    items = get_category_items_by_id(db, category_id=category_id)
    for item in items:
        delete_item_by_id(db, item.id)
    obj = db.query(models.Category).filter(models.Category.id == category_id).first()
    try:
        db.delete(obj)
        db.commit()
    except:
        return None
    return obj


def delete_category_by_name(db: Session, category_name: str):
    items = get_category_items_by_name(db, category_name=category_name)
    for item in items:
        delete_item_by_id(db, item.id)
    obj = db.query(models.Category).filter(models.Category.name == category_name).first()
    try:
        db.delete(obj)
        db.commit()
    except:
        return None
    return obj


def get_categories(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.Category).offset(skip).limit(limit).all()


def create_category(db: Session, category: schemas.CategoryCreate):
    db_category = models.Category(name=category.name)
    db.add(db_category)
    db.commit()
    db.refresh(db_category)
    return db_category


def get_category_items_by_id(db: Session, category_id: int, skip: int = 0, limit: int = 100):
    return db.query(models.Item).filter(models.Item.category_id == category_id).offset(skip).limit(limit).all()


def get_category_items_by_name(db: Session, category_name: str, skip: int = 0, limit: int = 100):
    category = get_category_by_name(db, category_name=category_name)
    return db.query(models.Item).filter(models.Item.category_id == category.id).offset(skip).limit(limit).all()


def create_category_items_by_id(db: Session, item: schemas.ItemCreate, category_id):
    db_item = models.Item(name=item.name, category_id=category_id,  price=item.price)
    db.add(db_item)
    db.commit()
    db.refresh(db_item)
    return db_item


def create_category_items_by_name(db: Session, item: schemas.ItemCreate, category_name):
    category = get_category_by_name(db, category_name=category_name)
    db_item = models.Item(name=item.name, category_id=category.id,  price=item.price)
    db.add(db_item)
    db.commit()
    db.refresh(db_item)
    return db_item


def get_item_by_id(db: Session, item_id: int):
    return db.query(models.Item).filter(models.Item.id == item_id).first()


def get_item_by_title(db: Session, item_title: str):
    return db.query(models.Item).filter(models.Item.name == item_title).first()


def delete_item_by_id(db: Session, item_id: int):
    item = get_item_by_id(db, item_id=item_id)
    try:
        db.delete(item)
        db.commit()
    except:
        return None
    return item


def delete_item_by_title(db: Session, item_title: str):
    item = get_item_by_title(db, item_title=item_title)
    delete_item_by_id(db, item_id=item.id)
    return item


def get_order_by_id(db: Session, order_id: int):
    return db.query(models.Order).filter(models.Order.id == order_id).first()


def create_order(db: Session, date: datetime, status: str, order: schemas.OrderCreate):
    db_order = models.Order(status=status, date=date)
    db.add(db_order)
    db.commit()
    db.refresh(db_order)
    return db_order


def delete_order(db: Session, order_id: int):
    order = get_order_by_id(db, order_id=order_id)
    for item in order.items:
        db.delete(item)
        db.commit()
    try:
        db.delete(order)
        db.commit()
    except:
        return None
    return order


def update_order(db: Session, order_id: int, status: str):
    order = get_order_by_id(db, order_id=order_id)
    order.status = status
    try:
        db.commit()
    except:
        return None
    return order


def add_item_by_id_to_order(db: Session, item: schemas.OrderItemBaseAdd,  order_id: int):
    order = get_order_by_id(db, order_id=order_id)
    for itemd in order.items:
        if itemd.item.id == item.item_id:
            item.quantity = itemd.quantity + item.quantity
            db.delete(itemd)
            db.commit()
    db_item = models.OrderItem(quantity=item.quantity, order_id=order_id, item_id=item.item_id)
    db.add(db_item)
    db.commit()
    db.refresh(db_item)
    return db_item


def add_item_by_title_to_order(db: Session, order_id: int, item: schemas.OrderItemBaseNameAdd):
    temp_item = get_item_by_title(db, item_title=item.name)
    db_item = models.OrderItem(quantity=item.quantity, order_id=order_id, item_id=temp_item.id)
    db.add(db_item)
    db.commit()
    db.refresh(db_item)
    return db_item


def update_item_by_id_to_order(db: Session, order_id: int, items: schemas.OrderItemBaseAdd):
    order = get_order_by_id(db, order_id=order_id)
    for item in order.items:
        if items.quantity > 0:
            if item.item.id == items.item_id:
                item.quantity = items.quantity
        else:
            db.delete(item)
            db.commit()
    try:
        db.commit()
    except:
        return None
    return order


def update_item_by_title_to_order(db: Session, order_id: int, items: schemas.OrderItemBaseNameAdd):
    order = get_order_by_id(db, order_id=order_id)
    for item in order.items:
        if items.quantity > 0:
            if item.item.name == items.name:
                item.quantity = items.quantity
        else:
            db.delete(item)
            db.commit()
    try:
        db.commit()
    except:
        return None
    return order


def create_category_synonym_by_id(db: Session, synonym: schemas.CategorySynonymBase,  category_id: int):
    db_item = models.CategorySynonym(synonym=synonym.synonym, category_id=category_id)
    db.add(db_item)
    db.commit()
    db.refresh(db_item)
    return db_item


def create_category_synonym_by_name(db: Session, synonym: schemas.CategorySynonymBase,  category_name: str):
    category = get_category_by_name(db=db, category_name=category_name)
    db_item = models.CategorySynonym(synonym=synonym.synonym, category_id=category.id)
    db.add(db_item)
    db.commit()
    db.refresh(db_item)
    return db_item


def get_category_synonym_by_id(db: Session, category_synonym_id: int):
    return db.query(models.CategorySynonym).filter(models.CategorySynonym.id == category_synonym_id).first()


def get_category_synonym_by_name(db: Session, category_synonym_name: str):
    return db.query(models.CategorySynonym).filter(models.CategorySynonym.synonym == category_synonym_name).first()


def delete_category_synonym_by_id(db: Session, category_synonym_id: int):
    synonym = get_category_synonym_by_id(db, category_synonym_id=category_synonym_id)
    try:
        db.delete(synonym)
        db.commit()
    except:
        return None
    return synonym


def delete_category_synonym_by_name(db: Session, category_synonym_name: str):
    synonym = get_category_synonym_by_name(db, category_synonym_name=category_synonym_name)
    try:
        db.delete(synonym)
        db.commit()
    except:
        return None
    return synonym

