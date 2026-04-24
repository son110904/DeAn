import hashlib
import secrets
from collections import defaultdict
from datetime import datetime

from sqlalchemy.orm import Session

from . import models, schemas

PASSWORD_SALT = "dean-static-salt"


def hash_password(password: str) -> str:
    return hashlib.sha256(f"{PASSWORD_SALT}:{password}".encode("utf-8")).hexdigest()


def create_user(db: Session, payload: schemas.UserRegister):
    db_user = models.User(
        name=payload.name.strip(),
        email=payload.email.lower(),
        password_hash=hash_password(payload.password),
    )
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user


def get_user_by_email(db: Session, email: str):
    return db.query(models.User).filter(models.User.email == email.lower()).first()


def authenticate_user(db: Session, payload: schemas.UserLogin):
    user = get_user_by_email(db, payload.email)
    if not user:
        return None
    if user.password_hash != hash_password(payload.password):
        return None
    return user


def issue_token(db: Session, user: models.User):
    user.token = secrets.token_urlsafe(32)
    db.commit()
    db.refresh(user)
    return user.token


def get_user_by_token(db: Session, token: str):
    return db.query(models.User).filter(models.User.token == token).first()


def get_transactions(db: Session, user_id: int, skip: int = 0, limit: int = 100):
    return (
        db.query(models.Transaction)
        .filter(models.Transaction.user_id == user_id)
        .order_by(models.Transaction.created_at.desc())
        .offset(skip)
        .limit(limit)
        .all()
    )


def create_transaction(db: Session, transaction: schemas.TransactionCreate, user_id: int):
    db_transaction = models.Transaction(
        amount=transaction.amount,
        category=transaction.category,
        type=transaction.type,
        note=transaction.note,
        date=transaction.date,
        user_id=user_id,
    )
    db.add(db_transaction)
    db.commit()
    db.refresh(db_transaction)
    return db_transaction


def get_transaction_by_id(db: Session, transaction_id: int, user_id: int):
    return (
        db.query(models.Transaction)
        .filter(models.Transaction.id == transaction_id, models.Transaction.user_id == user_id)
        .first()
    )


def update_transaction(
    db: Session,
    transaction_id: int,
    payload: schemas.TransactionCreate,
    user_id: int,
):
    db_transaction = get_transaction_by_id(db, transaction_id, user_id)
    if not db_transaction:
        return None

    db_transaction.amount = payload.amount
    db_transaction.category = payload.category
    db_transaction.type = payload.type
    db_transaction.note = payload.note
    db_transaction.date = payload.date
    db.commit()
    db.refresh(db_transaction)
    return db_transaction


def delete_transaction(db: Session, transaction_id: int, user_id: int):
    db_transaction = get_transaction_by_id(db, transaction_id, user_id)
    if not db_transaction:
        return False

    db.delete(db_transaction)
    db.commit()
    return True


def get_monthly_statistics(db: Session, user_id: int):
    rows = (
        db.query(models.Transaction)
        .filter(models.Transaction.user_id == user_id)
        .order_by(models.Transaction.created_at.asc())
        .all()
    )
    grouped = defaultdict(lambda: {"income": 0, "expense": 0})

    for row in rows:
        date_value = _extract_transaction_date(row)
        if not date_value:
            continue
        month_key = date_value[:7]
        if row.type.lower() == "income":
            grouped[month_key]["income"] += row.amount
        else:
            grouped[month_key]["expense"] += row.amount

    return [
        schemas.MonthlyStatistic(month=month, income=value["income"], expense=value["expense"])
        for month, value in grouped.items()
    ]


def get_daily_summary(db: Session, user_id: int, month: str):
    rows = (
        db.query(models.Transaction)
        .filter(models.Transaction.user_id == user_id, models.Transaction.type.ilike("expense"))
        .all()
    )

    grouped = defaultdict(int)
    for row in rows:
        date_value = _extract_transaction_date(row)
        if not date_value:
            continue
        if month and date_value[:7] != month:
            continue
        grouped[date_value] += row.amount

    return [
        schemas.DailySpending(date=date_key, amount=amount)
        for date_key, amount in sorted(grouped.items())
    ]


def upsert_budget(db: Session, payload: schemas.BudgetCreate, user_id: int):
    budget = (
        db.query(models.Budget)
        .filter(
            models.Budget.user_id == user_id,
            models.Budget.category == payload.category,
        )
        .first()
    )
    if budget:
        budget.limit_amount = payload.limit_amount
    else:
        budget = models.Budget(
            category=payload.category,
            limit_amount=payload.limit_amount,
            user_id=user_id,
        )
        db.add(budget)

    db.commit()
    db.refresh(budget)
    return budget


def get_budgets_with_spending(db: Session, user_id: int):
    budgets = (
        db.query(models.Budget)
        .filter(models.Budget.user_id == user_id)
        .order_by(models.Budget.category.asc())
        .all()
    )
    transactions = (
        db.query(models.Transaction)
        .filter(models.Transaction.user_id == user_id, models.Transaction.type.ilike("expense"))
        .all()
    )

    spending_by_category = defaultdict(int)
    for transaction in transactions:
        category = (transaction.category or "").strip()
        if category:
            spending_by_category[category] += transaction.amount

    return [
        schemas.BudgetRead(
            id=budget.id,
            category=budget.category,
            limit_amount=budget.limit_amount,
            current_spent=spending_by_category.get(budget.category, 0),
        )
        for budget in budgets
    ]


def get_budget_by_id(db: Session, budget_id: int, user_id: int):
    return (
        db.query(models.Budget)
        .filter(models.Budget.id == budget_id, models.Budget.user_id == user_id)
        .first()
    )


def update_budget(db: Session, budget_id: int, payload: schemas.BudgetCreate, user_id: int):
    budget = get_budget_by_id(db, budget_id, user_id)
    if not budget:
        return None

    budget.category = payload.category
    budget.limit_amount = payload.limit_amount
    db.commit()
    db.refresh(budget)
    return budget


def delete_budget(db: Session, budget_id: int, user_id: int):
    budget = get_budget_by_id(db, budget_id, user_id)
    if not budget:
        return False
    db.delete(budget)
    db.commit()
    return True


def analyze_qr_content(content: str):
    text = (content or "").strip()
    amount = 0
    for token in text.replace(",", " ").split():
        digits = "".join(ch for ch in token if ch.isdigit())
        if digits:
            amount = max(amount, int(digits))

    return schemas.TransactionCreate(
        amount=amount,
        category="Khac",
        type="expense",
        note=text[:200] if text else None,
        date=datetime.now().date().isoformat(),
    )


def _extract_transaction_date(row: models.Transaction):
    if row.date:
        return row.date.split("T")[0]
    created_at = row.created_at
    if isinstance(created_at, str):
        try:
            created_at = datetime.fromisoformat(created_at)
        except ValueError:
            return ""
    if created_at:
        return created_at.strftime("%Y-%m-%d")
    return ""
