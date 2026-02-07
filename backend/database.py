import os
from pathlib import Path

from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker


def _build_database_url() -> str:
    configured_url = os.getenv("FINANCE_DB_URL")
    if configured_url:
        return configured_url

    repo_root = Path(__file__).resolve().parent.parent
    db_path = repo_root / "finance.db"
    return f"sqlite:///{db_path.as_posix()}"


SQLALCHEMY_DATABASE_URL = _build_database_url()

engine = create_engine(
    SQLALCHEMY_DATABASE_URL,
    connect_args={"check_same_thread": False},
)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
