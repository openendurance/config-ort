# config-ort Documentation and Knowledge

Project documentation for `config-ort`.

This documentation set is the shared home for the project's durable knowledge. It serves human and agent operators: engineers need a nearby source of truth for architecture, standards, and implementation guidance, and agents use it as context and guardrails for their work.

## Why This Exists

`config-ort` is the shared OSS Review Toolkit (ORT) license-compliance configuration for the Open Endurance Initiative — the license categorization, policy rules, and package curations that every OEI repository's CI references to decide what's acceptable in its dependency tree. The documentation here explains how that configuration is structured, how downstream repos actually consume it, and the reasoning behind decisions already made, so people do not have to rediscover the same context every time they touch the work.

The top-level pages orient you, the reference sections explain how the system works, and the wiki space captures living knowledge that is still being shaped.

## Structure

| Directory    | Purpose                                                                                               |
| ------------ | ----------------------------------------------------------------------------------------------------- |
| `adr/`       | Architecture Decision Records — immutable once accepted; record why decisions were made               |
| `arch/`      | Current-state reference — system structure, contracts, and constraints; updated as the system evolves |
| `generated/` | Agent-generated output — do not hand-edit; other generated docs live here                             |
| `ops/`       | Operational reference — runbooks, testing strategy, health checks                                     |
| `registry/`  | Living registries — continuously updated tracking artifacts                                           |
| `wiki/`      | Living wiki space — working knowledge, migrated reference material, and evolving narrative content    |

## Entry Point

Start with `TOC.md` for a navigable index of all documents.

## Update Contract

- `adr/` — append only; never modify an accepted ADR
- `arch/` — update in place as the system changes; keep current
- `generated/` — written by agents; do not edit manually; re-run the originating skill to regenerate
- `ops/` — update when operational procedures change
- `registry/` — update continuously as findings are logged or resolved
- `wiki/` — update as living knowledge and migrated reference material is added or reshaped

## Conventions

- Paths within the documentation tree are relative to `/docs`; paths outside it are repo-relative
- Update `TOC.md` whenever a document is added, removed, or renamed
- Add a `TOC.md` entry for every new registry or ADR; leave section headers present even when empty
- Each subdirectory has its own README with authorship and update guidance, including `wiki/`
