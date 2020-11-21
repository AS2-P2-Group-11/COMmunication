from typing import List, Dict

from fastapi import Depends, FastAPI, HTTPException
from sqlalchemy.orm import Session

import crud, models, schemas
from database import SessionLocal, engine

models.Base.metadata.create_all(bind=engine)

app = FastAPI()


# Dependency
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


@app.get("/categories/", response_model=List[schemas.Category])
def read_categories(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    categories = crud.get_categories(db, skip=skip, limit=limit)
    return categories

@app.get("/category_by_id/{category_id}", response_model=schemas.Category)
def read_category_by_id(category_id: int, db: Session = Depends(get_db)):
    category = crud.get_category_by_id(db, category_id=category_id)
    if category is None:
        raise HTTPException(status_code=404, detail="Category not found")
    return category


@app.get("/category_by_name/{category_name}", response_model=schemas.Category)
def read_category_by_id(category_name: str, db: Session = Depends(get_db)):
    category = crud.get_category_by_name(db, category_name=category_name)
    if category is None:
        raise HTTPException(status_code=404, detail="Category not found")
    return category


@app.delete("/category_by_id/{category_id}", response_model=schemas.Category, status_code=200)
def remove_category_by_id(category_id: int, db: Session = Depends(get_db)):
    category = crud.delete_category_by_id(db, category_id=category_id)
    if category is None:
        raise HTTPException(status_code=404, detail="Category not found")
    return category


@app.delete("/category_by_name/{category_name}", response_model=schemas.Category, status_code=200)
def remove_category_by_name(category_name: str, db: Session = Depends(get_db)):
    category = crud.delete_category_by_name(db, category_name=category_name)
    if category is None:
        raise HTTPException(status_code=404, detail="Category not found")
    return category


@app.post("/categories/", response_model=schemas.Category, status_code=201)
def write_category(category: schemas.CategoryCreate, db: Session = Depends(get_db)):
    return crud.create_category(db=db, category=category)


@app.get("/category_by_id/{category_id}/items", response_model=List[schemas.Item])
def read_category_items_by_category_id(category_id, skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    items = crud.get_category_items_by_id(db, category_id=category_id, skip=skip, limit=limit)
    return items


@app.post("/category_by_id/{category_id}/items", response_model=schemas.Item)
def write_category_items_by_category_id(category_id, item: schemas.ItemCreate, db: Session = Depends(get_db)):
    items = crud.create_category_items_by_id(db, category_id=category_id, item=item)
    return items


@app.get("/category_by_name/{category_name}/items", response_model=List[schemas.Item])
def read_category_items_by_category_name(category_name, skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    items = crud.get_category_items_by_name(db, category_name=category_name, skip=skip, limit=limit)
    return items


@app.post("/category_by_name/{category_name}/items", response_model=schemas.Item)
def write_category_items_by_category_name(category_name, item: schemas.ItemCreate, db: Session = Depends(get_db)):
    items = crud.create_category_items_by_name(db, category_name=category_name, item=item)
    return items


@app.get("/item_by_id/{item_id}", response_model=schemas.Item)
def read_item_by_id(item_id: int, db: Session = Depends(get_db)):
    item = crud.get_item_by_id(db, item_id=item_id)
    if item is None:
        raise HTTPException(status_code=404, detail="Item not found")
    return item


@app.delete("/item_by_id/{item_id}", response_model=schemas.Item, status_code=200)
def remove_item_by_id(item_id: int, db: Session = Depends(get_db)):
    item = crud.delete_item_by_id(db, item_id=item_id)
    if item is None:
        raise HTTPException(status_code=404, detail="Item not found")
    return item


@app.get("/item_by_title/{item_title}", response_model=schemas.Item)
def read_item_by_title(item_title: str, db: Session = Depends(get_db)):
    item = crud.get_item_by_title(db, item_title=item_title)
    if item is None:
        raise HTTPException(status_code=404, detail="Item not found")
    return item


@app.delete("/item_by_title/{item_title}", response_model=schemas.Item, status_code=200)
def remove_item_by_title(item_title: str, db: Session = Depends(get_db)):
    item = crud.delete_item_by_title(db, item_title=item_title)
    if item is None:
        raise HTTPException(status_code=404, detail="Item not found")
    return item

