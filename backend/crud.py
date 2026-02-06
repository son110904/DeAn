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
        user_id=user_id,
    )
    db.add(db_transaction)
    db.commit()
    db.refresh(db_transaction)
    return db_transaction


def get_monthly_statistics(db: Session, user_id: int):
    rows = (
        db.query(models.Transaction)
        .filter(models.Transaction.user_id == user_id)
        .order_by(models.Transaction.created_at.asc())
        .all()
    )
    grouped = defaultdict(lambda: {"income": 0, "expense": 0})

    for row in rows:
        created_at = row.created_at
        if isinstance(created_at, str):
            created_at = datetime.fromisoformat(created_at)
        month_key = created_at.strftime("%Y-%m")
        if row.type.lower() == "income":
            grouped[month_key]["income"] += row.amount
        else:
            grouped[month_key]["expense"] += row.amount

    return [
        schemas.MonthlyStatistic(month=month, income=value["income"], expense=value["expense"])
        for month, value in grouped.items()
    ]
