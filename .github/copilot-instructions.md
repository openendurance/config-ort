# GitHub Copilot Instructions

## What this repo is

Shared OSS Review Toolkit (ORT) license-compliance configuration for the
Open Endurance Initiative (OEI). Every OEI repository's CI references this
repo's files (via a pinned git tag, through ORT's `ort-config-repository`
mechanism) to decide which open-source licenses are acceptable in their
dependency trees. There is no application code here — only configuration.

**This repo is high blast-radius relative to its size.** A change to
`src/curations.yml` or `src/evaluator.rules.kts` can silently change what
passes or fails CI in every other OEI repository the next time they pull a
new tag. Treat edits here with more caution than the line count would
suggest.

## File map

| File                              | Purpose                                                                                                                                                                                           |
| --------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `src/config.yml`                  | Global ORT analyzer/evaluator settings                                                                                                                                                            |
| `src/license-classifications.yml` | License categories (permissive, copyleft, etc.) — small and hand-maintained, not auto-generated                                                                                                   |
| `src/evaluator.rules.kts`         | Policy logic — maps categories to Approve/Flag/Deny. **Kotlin.** Nobody on the team currently knows Kotlin well; be conservative, explain changes clearly, and prefer minimal diffs over rewrites |
| `src/curations.yml`               | Corrections to _wrong or missing_ package license metadata — not a policy tool                                                                                                                    |
| `src/resolutions.yml`             | Documented exceptions to a specific flagged violation on a specific package/version                                                                                                               |

**Do not conflate `curations.yml` and `license-classifications.yml`.** If a
package's license is correctly detected but the question is whether OEI
accepts that license, the fix belongs in `license-classifications.yml`
(and/or `evaluator.rules.kts`). If the license data itself is wrong, the fix
belongs in `curations.yml`. Putting a policy decision in the wrong file is a
functional bug, not a style issue — it produces the correct outcome for the
wrong reason and breaks on the next related change.

## Branching and commits

- GitHub Flow: feature branches → PR into `develop` → `develop` → `main`.
  Breaking changes never land directly on `main`.
- **Every commit requires both a DCO sign-off and a cryptographic
  signature** — these are two unrelated mechanisms with similarly-named
  flags, and both are independently enforced as required checks:
    - `git commit -s` — DCO sign-off (adds `Signed-off-by:` trailer)
    - `git commit -S` — cryptographic signature (GPG or SSH, verified against
      a key registered on the committer's GitHub account as a _signing_ key)
    - In practice: `git commit -S -s` for both at once, or configure
      `commit.gpgsign true` plus always passing `-s`.
- If generating commits on someone's behalf, do not assume either is
  configured — check `git config --get commit.gpgsign` first, and surface
  the DCO/signature requirement rather than silently producing a commit
  that will fail CI.

## Tooling

- Package manager: `pnpm` only (`preinstall` enforces this).
- Formatting/linting is split by file type, not handled by one tool:
    - JSON/JSONC → Biome (`pnpm exec biome lint` / `biome format`)
    - YAML/Markdown → Prettier (`pnpm exec prettier`)
    - `.kts` → unhandled by either, intentionally, pending future Kotlin
      tooling
- `pnpm lint` runs both checks together; `pnpm format` fixes both.
- No test suite, no build step, no TypeScript — do not add `test`,
  `typecheck`, or `build` scripts without first confirming they're actually
  needed; this repo's CI assumes none of these exist.

## Releases

- [Changesets](https://github.com/changesets/changesets) — any change that
  should ship in a release needs `pnpm changeset`.
- Published to npm as `@openendurance/config-ort` with provenance
  attestation enabled (`publishConfig.provenance: true`). Changes to
  `publishConfig`, the release workflow, or anything affecting what gets
  published should be treated as sensitive — they affect the verifiable
  link between the published package and the commit that built it.

## This is a public OSS repository

Apache-2.0 licensed. Contribution process, code of conduct, and security
reporting are defined at the org level, not duplicated here — see
[openendurance/.github](https://github.com/openendurance/.github). Changes
to `src/curations.yml` and `src/evaluator.rules.kts` specifically require
CODEOWNERS review and should not be treated as routine.
