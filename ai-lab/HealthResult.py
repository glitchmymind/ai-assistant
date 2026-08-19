from dataclasses import dataclass

@dataclass
class HealthResult:
    available: bool
    version: str