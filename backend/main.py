import re

from fastapi import Body, Depends, FastAPI, Header, HTTPException, Query, status
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
    auth_value = (authorization or "").strip()
    if not auth_value:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Unauthorized")

    # Accept case-insensitive bearer prefix and tolerate duplicate prefix from legacy clients.
    token = auth_value
    for _ in range(2):
        if token.lower().startswith("bearer"):
            token = re.sub(r"^bearer\s+", "", token, flags=re.IGNORECASE).strip()

    if ((token.startswith('"') and token.endswith('"'))
            or (token.startswith("'") and token.endswith("'"))):
        token = token[1:-1].strip()

    if not token:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Unauthorized")

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


@app.put("/transactions/{transaction_id}", response_model=schemas.TransactionRead)
def update_transaction(
    transaction_id: int,
    payload: schemas.TransactionCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user),
):
    transaction = crud.update_transaction(db, transaction_id, payload, user_id=current_user.id)
    if not transaction:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Transaction not found")
    return transaction


@app.delete("/transactions/{transaction_id}")
def delete_transaction(
    transaction_id: int,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user),
):
    deleted = crud.delete_transaction(db, transaction_id, user_id=current_user.id)
    if not deleted:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Transaction not found")
    return {"ok": True}


@app.get("/statistics/monthly", response_model=list[schemas.MonthlyStatistic])
def get_monthly_statistics(
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user),
):
    return crud.get_monthly_statistics(db, user_id=current_user.id)


@app.get("/summary/daily", response_model=list[schemas.DailySpending])
def get_daily_summary(
    month: str = Query(default=""),
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user),
):
    return crud.get_daily_summary(db, user_id=current_user.id, month=month)


@app.get("/budgets", response_model=list[schemas.BudgetRead])
def get_budgets(
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user),
):
    return crud.get_budgets_with_spending(db, user_id=current_user.id)


@app.post("/budgets")
def save_budget(
    payload: schemas.BudgetCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user),
):
    budget = crud.upsert_budget(db, payload, user_id=current_user.id)
    return {"id": budget.id}


@app.put("/budgets/{budget_id}", response_model=schemas.BudgetRead)
def update_budget(
    budget_id: int,
    payload: schemas.BudgetCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user),
):
    try:
        budget = crud.update_budget(db, budget_id, payload, user_id=current_user.id)
    except IntegrityError:
        db.rollback()
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Budget category already exists")

    if not budget:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Budget not found")
    # Return with current_spent computed
    budgets = crud.get_budgets_with_spending(db, user_id=current_user.id)
    for item in budgets:
        if item.id == budget.id:
            return item
    return schemas.BudgetRead(id=budget.id, category=budget.category, limit_amount=budget.limit_amount, current_spent=0)


@app.delete("/budgets/{budget_id}")
def delete_budget(
    budget_id: int,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user),
):
    deleted = crud.delete_budget(db, budget_id, user_id=current_user.id)
    if not deleted:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Budget not found")
    return {"ok": True}


@app.post("/qr/analyze", response_model=schemas.TransactionCreate)
def analyze_qr(
    payload: str = Body(default="", embed=False),
):
    return crud.analyze_qr_content(payload)
