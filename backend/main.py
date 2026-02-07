from fastapi import Depends, FastAPI, Header, HTTPException, status
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session

from . import crud, models, schemas
from .database import Base, engine, get_db, migrate_legacy_schema

migrate_legacy_schema()
Base.metadata.create_all(bind=engine)

app = FastAPI(title="Finance API")


def get_current_user(
    authorization: str = Header(default=""),
    db: Session = Depends(get_db),
):
    if not authorization.startswith("Bearer "):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Unauthorized")

    token = authorization.replace("Bearer ", "", 1).strip()
    user = crud.get_user_by_token(db, token)
    if not user:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token")
    return user


@app.post("/auth/register", response_model=schemas.AuthResponse)
def register(payload: schemas.UserRegister, db: Session = Depends(get_db)):
    try:
        user = crud.create_user(db, payload)
    except IntegrityError:
        db.rollback()
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Email already exists")

    token = crud.issue_token(db, user)
    return schemas.AuthResponse(token=token, user=user)


@app.post("/auth/login", response_model=schemas.AuthResponse)
def login(payload: schemas.UserLogin, db: Session = Depends(get_db)):
    user = crud.authenticate_user(db, payload)
    if not user:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid credentials")

    token = crud.issue_token(db, user)
    return schemas.AuthResponse(token=token, user=user)


@app.get("/transactions", response_model=list[schemas.TransactionRead])
def list_transactions(
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user),
):
    return crud.get_transactions(db, user_id=current_user.id)


@app.post("/transactions", response_model=schemas.TransactionRead)
def create_transaction(
    payload: schemas.TransactionCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user),
):
    return crud.create_transaction(db, payload, user_id=current_user.id)


@app.get("/statistics/monthly", response_model=list[schemas.MonthlyStatistic])
def get_monthly_statistics(
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user),
):
    return crud.get_monthly_statistics(db, user_id=current_user.id)
