from __future__ import annotations

import os
from dataclasses import dataclass
from pathlib import Path

from dotenv import load_dotenv


BASE_DIR = Path(__file__).resolve().parents[1]
load_dotenv(BASE_DIR / ".env", override=False)


def _get_env(name: str, default: str | None = None) -> str:
    value = os.getenv(name, default)
    if value is None:
        raise ValueError(f"Missing required environment variable: {name}")
    return value


@dataclass(frozen=True)
class MysqlSettings:
    host: str
    port: int
    database: str
    username: str
    password: str


@dataclass(frozen=True)
class ClickHouseSettings:
    host: str
    port: int
    database: str
    username: str
    password: str


@dataclass(frozen=True)
class PipelineSettings:
    environment: str
    timezone: str
    log_dir: Path
    data_dir: Path
    mysql: MysqlSettings
    clickhouse: ClickHouseSettings

    def ensure_directories(self) -> None:
        self.log_dir.mkdir(parents=True, exist_ok=True)
        self.data_dir.mkdir(parents=True, exist_ok=True)
        (self.data_dir / "exports").mkdir(parents=True, exist_ok=True)


def load_settings() -> PipelineSettings:
    return PipelineSettings(
        environment=_get_env("PIPELINE_ENV", "local"),
        timezone=_get_env("PIPELINE_TIMEZONE", "Asia/Seoul"),
        log_dir=(BASE_DIR / _get_env("PIPELINE_LOG_DIR", "./logs")).resolve(),
        data_dir=(BASE_DIR / _get_env("PIPELINE_DATA_DIR", "./data")).resolve(),
        mysql=MysqlSettings(
            host=_get_env("MYSQL_HOST"),
            port=int(_get_env("MYSQL_PORT", "3306")),
            database=_get_env("MYSQL_DATABASE"),
            username=_get_env("MYSQL_USERNAME"),
            password=_get_env("MYSQL_PASSWORD"),
        ),
        clickhouse=ClickHouseSettings(
            host=_get_env("CLICKHOUSE_HOST"),
            port=int(_get_env("CLICKHOUSE_PORT", "8123")),
            database=_get_env("CLICKHOUSE_DATABASE"),
            username=_get_env("CLICKHOUSE_USERNAME", "default"),
            password=_get_env("CLICKHOUSE_PASSWORD", ""),
        ),
    )
