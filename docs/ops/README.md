# Operations

Operational reference documentation for `config-ort`.

## What Belongs Here

Documents that answer "how do we run, test, and debug this system?" — runbooks, testing strategy, and operational procedures. These are reference docs for engineers and agents performing operational work.

This repo has no running system in the usual sense — no deployment, no test suite. "Operations" here means: the release process (Changesets → tag → npm publish with provenance), how to verify a policy change is correct before merging given there's no automated test suite to catch a bad `curations.yml` or `evaluator.rules.kts` edit, and incident response for when a change here breaks CI in a downstream repo.

System structure belongs in `arch/`.

Decisions about operational approach belong in `adr/`.

## Contents

| Document | Purpose |
| -------- | ------- |

No operational documents are currently tracked here.

## Authorship

Human-maintained. Agents may append findings or incident notes as part of a work increment, but procedural changes require human review.

## Update Contract

Update when procedures change. An outdated runbook is an incident waiting to happen — if a procedure has drifted from the document, fix the document immediately.

If a significant operational decision was made (e.g. changing the testing approach, adopting a new deployment pattern), open an ADR before updating the affected document here.
