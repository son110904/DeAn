from fastapi import Depends, FastAPI
from sqlalchemy.orm import Session

from . import crud, models, schemas
from .database import Base, engine, get_db

Base.metadata.create_all(bind=engine)

app = FastAPI(title="Finance API")


@app.get("/transactions", response_model=list[schemas.TransactionRead])
def list_transactions(db: Session = Depends(get_db)):
    return crud.get_transactions(db)


@app.post("/transactions", response_model=schemas.TransactionRead)
def create_transaction(payload: schemas.TransactionCreate, db: Session = Depends(get_db)):
    return crud.create_transaction(db, payload)
