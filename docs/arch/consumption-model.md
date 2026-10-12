# Consumption Model

How downstream OEI repositories actually consume this repo's configuration, and why two independent paths exist.

## The two paths

|                     | GitHub Actions (primary)                                                                                                                                                              | npm (secondary)                                                                           |
| ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------- |
| **Mechanism**       | [`oss-review-toolkit/ort-ci-github-action`](https://github.com/oss-review-toolkit/ort-ci-github-action)'s `ort-config-repository` input clones this repo directly at a pinned git ref | `@openendurance/config-ort` published to the npm registry; files read from `node_modules` |
| **What it's for**   | The actual CI enforcement path — this is how license-compliance gating happens in practice                                                                                            | Any tooling that needs to read `*.yml`/`*.kts` directly from Node, outside of an ORT run  |
| **Versioning unit** | A git tag                                                                                                                                                                             | An npm semver version                                                                     |

### GitHub Actions path (primary)

```yaml
- uses: oss-review-toolkit/ort-ci-github-action@v1
  with:
      ort-config-repository: https://github.com/openendurance/config-ort
      # pinned to a specific tag, e.g. @v1.2.0 — never @develop or @main
```

The Action clones this repo at the referenced ref into the directory ORT
reads its global configuration from. ORT's own default behavior looks for
`config.yml`, `curations.yml`, `license-classifications.yml`,
`evaluator.rules.kts`, and `resolutions.yml` **directly at the top level**
of that location — it does not traverse into subdirectories. This is why
these five files live at this repo's root rather than under `src/` the way
other OEI repos organize things: `src/config.yml` etc. is invisible to the
Action's default lookup, and files placed there are silently never read.
This isn't a style preference — it's a hard constraint imposed by ORT
itself.

This is the path that actually gates pull requests across OEI — if a
repo's CI references this repo at all, it's almost certainly through this
mechanism.

**Pinning matters.** Referencing a branch (`main`, `develop`) instead of a
tag means a policy change here takes effect in every downstream repo's next
CI run with zero visibility into what changed or when — effectively an
unreviewed, silent policy update landing everywhere at once. Pinning to a
tag means a downstream repo only picks up a change when someone
deliberately bumps the pin, which is itself a reviewable PR.

### npm path (secondary)

```sh
pnpm add @openendurance/config-ort
```

Published with [provenance attestation](https://docs.npmjs.com/generating-provenance-statements)
enabled, so the published package is cryptographically verifiable against
the exact commit and GitHub Actions run that built it. Exists for cases
where something needs these files as data — outside of an actual ORT run —
without re-implementing a git clone.

As of this writing, no OEI tooling actually consumes this path yet. It
exists because the cost of publishing alongside the primary path is low,
not because of a known current consumer. If that changes, document the
actual consumer here.

## The sync risk

These two paths are **versioned independently** — nothing technically
enforces that an npm version and a git tag correspond to the same content.
A release that publishes a new npm version without also tagging the
matching commit (or vice versa) means a repo consuming via GitHub Actions
and a repo consuming via npm could be looking at genuinely different policy
states at the same point in time, with no error or warning from either
side — it would just silently diverge.

**At release time:** a release is only complete once both the git tag and
the npm publish point at the same commit. The release workflow
(`.github/workflows/release.yml`) publishes to npm as part of the
Changesets flow; confirm the corresponding git tag exists and matches
before considering a release done. See [`ops/`](../ops/README.md) for the
release runbook once one exists.

## Who's affected by a change here

Any OEI repository whose CI references `ort-config-repository` at a tag
equal to or newer than a change's release is affected immediately on its
next CI run, with no action required on that repo's part. This is the
entire point of the shared-config model — and also why changes to
`curations.yml` and `evaluator.rules.kts` specifically require CODEOWNERS
review rather than being self-service (see the root
[`README.md`](../../README.md) and the org-wide
[CONTRIBUTING guide](https://github.com/openendurance/.github/blob/main/CONTRIBUTING.md)).
