from __future__ import annotations

import logging
from pathlib import Path

from common.config import PipelineSettings


def get_logger(name: str, settings: PipelineSettings) -> logging.Logger:
    logger = logging.getLogger(name)
    if logger.handlers:
        return logger

    settings.ensure_directories()

    logger.setLevel(logging.INFO)
    logger.propagate = False

    formatter = logging.Formatter(
        fmt="%(asctime)s [%(levelname)s] %(name)s - %(message)s",
        datefmt="%Y-%m-%d %H:%M:%S",
    )

    console_handler = logging.StreamHandler()
    console_handler.setFormatter(formatter)

    file_path = Path(settings.log_dir) / f"{name}.log"
    file_handler = logging.FileHandler(file_path, encoding="utf-8")
    file_handler.setFormatter(formatter)

    logger.addHandler(console_handler)
    logger.addHandler(file_handler)
    return logger
