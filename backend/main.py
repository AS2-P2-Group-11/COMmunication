from typing import List, Dict

from fastapi import Depends, FastAPI, HTTPException
from sqlalchemy.orm import Session

import crud, models, schemas, common, datetime
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


@app.get("/item_by_name/{item_name}", response_model=schemas.Item)
def read_item_by_title(item_title: str, db: Session = Depends(get_db)):
    item = crud.get_item_by_title(db, item_title=item_title)
    if item is None:
        raise HTTPException(status_code=404, detail="Item not found")
    return item


@app.delete("/item_by_name/{item_name}", response_model=schemas.Item, status_code=200)
def remove_item_by_title(item_title: str, db: Session = Depends(get_db)):
    item = crud.delete_item_by_title(db, item_title=item_title)
    if item is None:
        raise HTTPException(status_code=404, detail="Item not found")
    return item


@app.get("/order/{order_id}", response_model=schemas.Order)
def read_order_by_id(order_id: int, db: Session = Depends(get_db)):
    order = crud.get_order_by_id(db, order_id=order_id)
    if order is None:
        raise HTTPException(status_code=404, detail="Item not found")
    return order


@app.post("/order", response_model=schemas.Order)
def write_order(order: schemas.OrderCreate, db: Session = Depends(get_db)):
    order = crud.create_order(db, order=order, date=datetime.datetime.now(), status=common.Status.submitted)
    return order


@app.delete("/order/{order_id}", response_model=schemas.Order, status_code=200)
def remove_order(order_id: int, db: Session = Depends(get_db)):
    order = crud.delete_order(db, order_id=order_id)
    if order is None:
        raise HTTPException(status_code=404, detail="Item not found")
    return order


@app.patch("/order/{order_id}", response_model=schemas.Order, status_code=200)
def update_oder(order: schemas.Order, order_id: int, db: Session = Depends(get_db)):
    order = crud.update_order(db, order_id=order_id, status=order.status)
    if order is None:
        raise HTTPException(status_code=404, detail="Item not found")
    return order


@app.post("/order/{order_id}/item_by_id")
def add_item_by_id_to_order(order_id, item: schemas.OrderItemBaseAdd, db: Session = Depends(get_db)):
    if item.quantity > 0:
        crud.add_item_by_id_to_order(db, item=item, order_id=order_id)
        return {"info": "item added to order"}
    else:
        raise HTTPException(status_code=400, detail="No item is added to order")


@app.post("/order/{order_id}/item_by_name")
def add_item_by_title_to_order(order_id, item: schemas.OrderItemBaseNameAdd, db: Session = Depends(get_db)):
    if item.quantity > 0:
        crud.add_item_by_title_to_order(db, item=item, order_id=order_id)
        return {"info": "item added to order"}
    else:
        raise HTTPException(status_code=400, detail="No item is added to order")


@app.patch("/order/{order_id}/item_by_id")
def update_item_by_id_to_order(order_id, item: schemas.OrderItemBaseAdd, db: Session = Depends(get_db)):
    if item.quantity >= 0:
        crud.update_item_by_id_to_order(db, items=item, order_id=order_id)
        return {"info": "item updated to order"}
    else:
        raise HTTPException(status_code=400, detail="No item is added to order")


@app.patch("/order/{order_id}/item_by_name")
def update_item_by_title_to_order(order_id, item: schemas.OrderItemBaseNameAdd, db: Session = Depends(get_db)):
    if item.quantity >= 0:
        crud.update_item_by_title_to_order(db, items=item, order_id=order_id)
        return {"info": "item updated to order"}
    else:
        raise HTTPException(status_code=400, detail="No item is added to order")


@app.post("/category_by_id/{category_id}/synonyms", response_model=schemas.CategorySynonym)
def write_category_synonyms_by_category_id(category_id, synonym: schemas.CategorySynonymCreate, db: Session = Depends(get_db)):
    synonyms = crud.create_category_synonym_by_id(db, category_id=category_id, synonym=synonym)
    return synonyms


@app.post("/category_by_name/{category_name}/synonyms", response_model=schemas.CategorySynonym)
def write_category_synonyms_by_category_name(category_name, synonym: schemas.CategorySynonymCreate, db: Session = Depends(get_db)):
    synonyms = crud.create_category_synonym_by_name(db, category_name=category_name, synonym=synonym)
    return synonyms