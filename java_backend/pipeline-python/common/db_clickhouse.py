from __future__ import annotations

from collections.abc import Sequence
from typing import Any

import clickhouse_connect

from common.config import PipelineSettings


def create_clickhouse_client(settings: PipelineSettings):
    clickhouse = settings.clickhouse
    return clickhouse_connect.get_client(
        host=clickhouse.host,
        port=clickhouse.port,
        username=clickhouse.username,
        password=clickhouse.password,
        database=clickhouse.database,
    )


def insert_rows(client, table_name: str, rows: Sequence[dict[str, Any]]) -> int:
    if not rows:
        return 0

    column_names = list(rows[0].keys())
    data = [[row.get(column_name) for column_name in column_names] for row in rows]
    client.insert(table=table_name, data=data, column_names=column_names)
    return len(rows)
