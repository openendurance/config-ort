# Architecture Decision Records

Immutable records of significant architectural decisions made during the life of `config-ort`.

## What Belongs Here

A decision warrants an ADR when:

- It has meaningful alternatives that were considered and rejected
- It carries consequences that future engineers need to understand
- It would be costly or risky to reverse without documented rationale

Implementation details, coding conventions, and operational procedures do not belong here — use `arch/` or `ops/` respectively.

## Authorship

ADRs may be authored by engineers, architects, or agents. Regardless of origin, an ADR must be reviewed and explicitly accepted by a human before its status is set to `Accepted`.

## Structure

Each ADR follows this template:

```markdown
# ADR-NNNN: {Title}

## Status

{Proposed | Accepted | Deprecated | Superseded by ADR-NNNN}

## Date

YYYY-MM-DD

## Context

What situation or constraint forced this decision?

## Decision

What was decided, and how is it applied?

## Consequences

### Positive

### Negative

## Implementation Notes

Specifics required to apply the decision correctly. Include fallback patterns, known edge cases, and any tooling that needs updating.
```

## Numbering

Files are named `NNNN-{kebab-title}.md` where `NNNN` is a zero-padded sequential integer starting at `0001`. Do not reuse numbers. Do not renumber existing ADRs.

## Immutability

Once an ADR is `Accepted`:

- Its context, decision, and consequences are frozen
- Corrections to factual errors are permitted with a dated note
- If a decision is reversed, open a new ADR and progress it through normal statuses (`Proposed` -> `Accepted`); then update the original ADR status to `Superseded by ADR-NNNN`

## Update Contract

Append only. The ADR log is a historical record — it reflects what was decided and when, not just what is currently true.
