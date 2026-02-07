from sqlalchemy import create_engine, inspect, text
from sqlalchemy.orm import declarative_base, sessionmaker

SQLALCHEMY_DATABASE_URL = "sqlite:///./finance.db"

engine = create_engine(
    SQLALCHEMY_DATABASE_URL,
    connect_args={"check_same_thread": False},
)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()


def migrate_legacy_schema():
    """Apply minimal schema fixes for pre-existing SQLite databases."""

    with engine.begin() as connection:
        inspector = inspect(connection)
        table_names = set(inspector.get_table_names())

        if "users" in table_names:
            user_columns = {column["name"] for column in inspector.get_columns("users")}
            if "token" not in user_columns:
                connection.execute(text("ALTER TABLE users ADD COLUMN token VARCHAR"))

        if "transactions" in table_names:
            transaction_columns = {
                column["name"] for column in inspector.get_columns("transactions")
            }
            if "user_id" not in transaction_columns:
                connection.execute(text("ALTER TABLE transactions ADD COLUMN user_id INTEGER"))
                connection.execute(
                    text(
                        "CREATE INDEX IF NOT EXISTS ix_transactions_user_id "
                        "ON transactions (user_id)"
                    )
                )


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
