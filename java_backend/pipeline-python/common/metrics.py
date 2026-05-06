from __future__ import annotations

from dataclasses import asdict, dataclass, field
from datetime import datetime, timezone


@dataclass
class JobMetrics:
    job_name: str
    target_date: str
    status: str = "RUNNING"
    source_count: int = 0
    loaded_count: int = 0
    error_count: int = 0
    started_at: datetime = field(default_factory=lambda: datetime.now(timezone.utc))
    finished_at: datetime | None = None
    error_message: str | None = None

    def mark_success(self, loaded_count: int) -> None:
        self.status = "SUCCESS"
        self.loaded_count = loaded_count
        self.finished_at = datetime.now(timezone.utc)

    def mark_failed(self, error_message: str) -> None:
        self.status = "FAILED"
        self.error_count = max(self.error_count, 1)
        self.error_message = error_message
        self.finished_at = datetime.now(timezone.utc)

    def elapsed_ms(self) -> int | None:
        if self.finished_at is None:
            return None
        delta = self.finished_at - self.started_at
        return int(delta.total_seconds() * 1000)

    def as_dict(self) -> dict:
        payload = asdict(self)
        payload["elapsed_ms"] = self.elapsed_ms()
        return payload
