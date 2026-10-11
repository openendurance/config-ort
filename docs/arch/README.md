# Architecture

Current-state reference documentation for `config-ort`.

## What Belongs Here

Documents that answer "how does this system work right now?" — structure, contracts, constraints, and integration points. These are living documents; update them as the system evolves.

This repo has no runtime architecture in the usual sense — no services, no API contracts. What belongs here instead: how downstream OEI repos actually consume these config files (the `ort-config-repository` mechanism, the npm package path, and how the two relate), and the structural contract between the five `src/` files themselves (what `curations.yml` is for versus `license-classifications.yml`, and why that distinction matters).

Decisions about _why_ the system is structured this way belong in `adr/`.

Operational procedures belong in `ops/`.

## Contents

| Document | Purpose |
| -------- | ------- |

No architecture documents are currently tracked here.

## Authorship

These documents are human-maintained. Agents may propose updates as part of a work increment, but a human operator is responsible for reviewing and committing changes. The _structure_ of these documents is an organizational standard and should NEVER be changed without explicit instruction by an operator, which should generally include justification, likely via an approved ADR.

## Update Contract

Update in place when the system changes. If a change represents a significant architectural shift, open an ADR first — the ADR records the decision, this directory reflects the outcome.

Do not let these documents drift from the implementation. Stale architecture docs are worse than no docs — they actively mislead.
