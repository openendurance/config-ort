# @openendurance/config-ort

Shared [OSS Review Toolkit (ORT)](https://oss-review-toolkit.org/ort/) license-compliance configuration for Open Endurance Initiative repositories — license categorization, policy rules, and package curations, defined once and consumed by every OEI repo's CI.

## Why this exists

OEI repositories gate pull requests on open source license compliance. Rather than each repo defining its own policy (or paying for a vendor's policy editor — see the history below if you're curious), this repo is the single source of truth: one place to approve a license, correct bad package metadata, or change enforcement rules, with every downstream repo picking up the change automatically.

## How repositories use this

**Primary path — GitHub Actions.** Any OEI repo's validation workflow references this repo directly via [`oss-review-toolkit/ort-ci-github-action`](https://github.com/oss-review-toolkit/ort-ci-github-action)'s `ort-config-repository` input, pinned to a released git tag (not `main` — see [Making changes](#making-changes) below for why that matters):

```yaml
- uses: oss-review-toolkit/ort-ci-github-action@v1
  with:
    ort-config-repository: https://github.com/openendurance/config-ort
    # pin to a specific tag once this repo has its first release
```

**Secondary path — npm.** Published as [`@openendurance/config-ort`](https://www.npmjs.com/package/@openendurance/config-ort) for any tooling that needs to read these files directly from Node rather than through the ORT Action.

Both paths exist independently — see the note in [Making changes](#making-changes) about keeping them in sync.

## What's here

All configuration lives in `src/`:

| File | Purpose |
|---|---|
| `config.yml` | Global ORT settings — analyzer and evaluator behavior |
| `license-classifications.yml` | License categories (permissive, copyleft, etc.) that `evaluator.rules.kts` acts on |
| `evaluator.rules.kts` | Policy logic — maps categories to Approve / Flag / Deny |
| `curations.yml` | Corrections for invalid or missing package license metadata |
| `resolutions.yml` | Documented, reviewed exceptions to specific flagged violations |

`license-classifications.yml` is the one most worth reading first if you're trying to understand *why* something was flagged — it's a small, deliberately-maintained list rather than an auto-generated one, and its categories (and the reasoning behind each) are documented inline in the file itself.

## Making changes

Changes to `curations.yml` and `evaluator.rules.kts` in particular affect every OEI repository's CI at once — they're CODEOWNERS-reviewed, not self-service. See the org-wide [CONTRIBUTING guide](https://github.com/openendurance/.github/blob/main/CONTRIBUTING.md) for the general process.

One thing specific to this repo worth knowing: it has two independent consumption paths (the git-tag reference above, and the npm package). Bumping the npm version without also tagging the corresponding git commit — or vice versa — means downstream repos using different consumption paths could silently see different policy states. Keep a release's npm publish and its git tag in lockstep.

## Development

```sh
pnpm install
pnpm lint    # Biome (JSON) + Prettier (YAML/Markdown)
pnpm format  # writes fixes for both
```

Branching follows the org-wide [GitHub Flow standard](https://github.com/openendurance/.github/blob/main/CONTRIBUTING.md#branch-organization): feature branches → `develop` → `main`. Releases are managed with [Changesets](https://github.com/changesets/changesets) — run `pnpm changeset` for any change that should trigger a release.

## License

Apache-2.0 — see [`LICENSE`](LICENSE). Community standards and reporting process are defined at the [org level](https://github.com/openendurance/.github).
