from sqlalchemy.orm import Session

import models, schemas


def get_category_by_id(db: Session, category_id: int):
    return db.query(models.Category).filter(models.Category.id == category_id).first()


def delete_category_by_id(db: Session, category_id: int):
    obj = db.query(models.Category).filter(models.Category.id == category_id).first()
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


def get_category_items_by_id(db: Session, category_id: int, skip: int=0, limit: int = 100):
    return db.query(models.Item).filter(models.Item.category_id == category_id).offset(skip).limit(limit).all()

