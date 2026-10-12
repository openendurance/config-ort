# Registry

Living registries of continuously tracked artifacts for `config-ort`.

## What Belongs Here

Documents that are neither point-in-time snapshots nor static reference material — registries are updated continuously as findings are logged, resolved, or escalated. They represent the current known state of a tracked concern.

Operational procedures belong in `ops/`.

Generated output belongs in `generated/`.

## Contents

| Document | Purpose |
| -------- | ------- |

No registries are currently tracked here.

## Authorship

Human and agent maintained. Agents executing multi-step or long-running pipelines are expected to update relevant registries as part of their output. Human operators are responsible for resolution status and escalation decisions.

## Update Contract

Update in place as findings are logged or resolved. A registry entry is never deleted — if a finding is resolved, mark it resolved with a date and reference the ADR or commit that closed it. The full history of findings is part of the record.

New registries may be added as new tracked concerns emerge. Add each to `TOC.md` when created.
