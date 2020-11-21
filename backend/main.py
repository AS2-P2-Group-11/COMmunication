from typing import List

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


@app.delete("/category_by_id/{category_id}", response_model=schemas.Category, status_code=200)
def remove_category_by_id(category_id: int, db: Session = Depends(get_db)):
    category = crud.delete_category_by_id(db, category_id=category_id)
    if category is None:
        raise HTTPException(status_code=404, detail="Category not found")
    return category


@app.post("/categories/", response_model=schemas.Category, status_code=201)
def write_category(category: schemas.CategoryCreate, db: Session = Depends(get_db)):
    return crud.create_category(db=db, category=category)


@app.get("/categories/{category_id}/items", response_model=List[schemas.Item])
def read_category_items(category_id, skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    items = crud.get_category_items_by_id(db, category_id=category_id, skip=skip, limit=limit)
    return items




