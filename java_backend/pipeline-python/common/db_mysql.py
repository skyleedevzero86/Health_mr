from __future__ import annotations

from typing import Any

from sqlalchemy import create_engine, text
from sqlalchemy.engine import Engine

from common.config import PipelineSettings


def create_mysql_engine(settings: PipelineSettings) -> Engine:
    mysql = settings.mysql
    url = (
        f"mysql+pymysql://{mysql.username}:{mysql.password}"
        f"@{mysql.host}:{mysql.port}/{mysql.database}"
    )
    return create_engine(url, pool_pre_ping=True)


def fetch_rows(engine: Engine, query: str, params: dict[str, Any] | None = None) -> list[dict[str, Any]]:
    with engine.connect() as connection:
        result = connection.execute(text(query), params or {})
        return [dict(row._mapping) for row in result]
