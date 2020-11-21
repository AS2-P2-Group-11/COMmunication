from sqlalchemy.orm import Session

import models, schemas


def get_category_by_id(db: Session, category_id: int):
    return db.query(models.Category).filter(models.Category.id == category_id).first()


def get_category_by_name(db: Session, category_name: str):
    return db.query(models.Category).filter(models.Category.name == category_name).first()


def delete_category_by_id(db: Session, category_id: int):
    obj = db.query(models.Category).filter(models.Category.id == category_id).first()
    try:
        db.delete(obj)
        db.commit()
    except:
        return None
    return obj


def delete_category_by_name(db: Session, category_name: str):
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
    db_item = models.Item(title=item.title, category_id=category_id,  price=item.price)
    db.add(db_item)
    db.commit()
    db.refresh(db_item)
    return db_item


def create_category_items_by_name(db: Session, item: schemas.ItemCreate, category_name):
    category = get_category_by_name(db, category_name=category_name)
    db_item = models.Item(title=item.title, category_id=category.id,  price=item.price)
    db.add(db_item)
    db.commit()
    db.refresh(db_item)
    return db_item


def get_item_by_id(db: Session, item_id: int):
    return db.query(models.Item).filter(models.Item.id == item_id).first()


def get_item_by_title(db: Session, item_title: str):
    return db.query(models.Item).filter(models.Item.title == item_title).first()


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