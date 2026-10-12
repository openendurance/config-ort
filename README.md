# @openendurance/config-ort

Shared [OSS Review Toolkit (ORT)](https://oss-review-toolkit.org/ort/) license-compliance configuration for Open Endurance Initiative repositories — license categorization, policy rules, and package curations, defined once and consumed by every OEI repo's CI.

## Why this exists

OEI repositories gate pull requests on open source license compliance. Rather than each repo defining its own policy (or paying for a vendor's policy editor — see the history below if you're curious), this repo is the single source of truth: one place to approve a license, correct bad package metadata, or change enforcement rules, with every downstream repo picking up the change automatically.

## How repositories use this

See [`docs/arch/`](docs/arch/README.md) for how downstream repos actually consume this config (the GitHub Actions / `ort-config-repository` path and the npm package path), and the risk of letting the two drift out of sync.

## What's here

All configuration lives at the repo root — not under `src/`, unlike other OEI repos. This is deliberate: ORT's default config lookup expects these exact filenames directly at the top level of whatever gets cloned via `ort-config-repository`, with no subdirectory support. See [`docs/arch/`](docs/arch/README.md) for the full explanation.

| File                          | Purpose                                                                            |
| ----------------------------- | ---------------------------------------------------------------------------------- |
| `config.yml`                  | Global ORT settings — analyzer and evaluator behavior                              |
| `license-classifications.yml` | License categories (permissive, copyleft, etc.) that `evaluator.rules.kts` acts on |
| `evaluator.rules.kts`         | Policy logic — maps categories to Approve / Flag / Deny                            |
| `curations.yml`               | Corrections for invalid or missing package license metadata                        |
| `resolutions.yml`             | Documented, reviewed exceptions to specific flagged violations                     |

`license-classifications.yml` is the one most worth reading first if you're trying to understand _why_ something was flagged — it's a small, deliberately-maintained list rather than an auto-generated one, and its categories (and the reasoning behind each) are documented inline in the file itself.

## Making changes

Changes to `curations.yml` and `evaluator.rules.kts` in particular affect every OEI repository's CI at once — they're CODEOWNERS-reviewed, not self-service. See the org-wide [CONTRIBUTING guide](https://github.com/openendurance/.github/blob/main/CONTRIBUTING.md) for the general process.

## Development

```sh
pnpm install
pnpm lint    # Biome (JSON) + Prettier (YAML/Markdown)
pnpm format  # writes fixes for both
```

Branching follows the org-wide [GitHub Flow standard](https://github.com/openendurance/.github/blob/main/CONTRIBUTING.md#branch-organization): feature branches → `develop` → `main`. Releases are managed with [Changesets](https://github.com/changesets/changesets) — run `pnpm changeset` for any change that should trigger a release.

## License

Apache-2.0 — see [`LICENSE`](LICENSE). Community standards and reporting process are defined at the [org level](https://github.com/openendurance/.github).
