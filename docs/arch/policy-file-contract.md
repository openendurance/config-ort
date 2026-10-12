# Policy File Contract

How the five policy files at this repo's root relate to each other as a
system — not just what each one does individually, but how a license goes
from "detected" to "Approved / Flagged / Denied."

## Data flow

```
config.yml
  → sets global thresholds (e.g. severeRuleViolationThreshold)

license-classifications.yml
  → categorizes known SPDX license ids into buckets
    (permissive, permissive-emerging, attribution-content,
     weak-copyleft, strong-copyleft, copyleft-with-linking-exception,
     public-domain)

evaluator.rules.kts
  → reads those categories, decides an outcome per category
    (Approve / Flag with hint or warning / Deny with error)
  → anything NOT in license-classifications.yml falls through to
    UNHANDLED_LICENSE, which is always an error — never silently approved

curations.yml  ─┐
resolutions.yml ─┴→ two separate escape hatches, see below
```

A license's path through this system in the common case: ORT's Analyzer
detects a package's license → `evaluator.rules.kts` looks up that license's
category in `license-classifications.yml` → the category determines the
outcome. Most licenses never touch `curations.yml` or `resolutions.yml` at
all — those exist for the exception cases below.

## The hard rule: `curations.yml` vs. `license-classifications.yml`

These are frequently confused, and conflating them is a **functional bug**,
not a style preference — it produces the right-looking outcome for the
wrong reason, and breaks the next time something related changes.

**`license-classifications.yml` (+ `evaluator.rules.kts`) answers:**
_"Given that a package's license is correctly identified as X, does OEI
accept X?"_ — a policy question. Changing a license's category changes the
outcome for every package that carries that license, everywhere, forever
(until changed again).

**`curations.yml` answers:** _"ORT detected this specific package's license
incorrectly or couldn't detect it at all — what's the correct license?"_ —
a data-correction question, scoped to one specific package (and often one
specific version). It never expresses a policy opinion about whether a
license is acceptable; it only corrects what license a package is said to
carry.

**Why the distinction matters structurally:** if a license you don't want
to allow org-wide gets curated into acceptance for one package instead of
being properly categorized, the next package with that same, correctly-
detected license sails through unflagged — the policy was never actually
set, you just patched around one symptom. Conversely, if a real metadata
error gets "fixed" by recategorizing the (wrong) detected license as
acceptable, you've just approved a license category you didn't mean to,
org-wide, to work around a data bug in one package.

**The test to apply:** is the license ORT reports for this package
_correct_?

- **Yes**, and the question is whether OEI accepts it → `license-classifications.yml` / `evaluator.rules.kts`
- **No**, ORT has the wrong license (or none) for this package → `curations.yml`

## Where `resolutions.yml` fits

Different from both of the above: `resolutions.yml` doesn't correct data
and doesn't change policy. It documents a **reviewed, one-off exception to
a specific flagged violation** — e.g., "yes, package X@1.2.3 is
strong-copyleft and would normally be denied, but we've acquired a
commercial license for this specific dependency, here's the record of
that." It accepts one outcome on one package/version without changing how
that license is treated anywhere else, and without claiming the detected
license is wrong.

| File                                                  | Changes...                                | Scope                                                           |
| ----------------------------------------------------- | ----------------------------------------- | --------------------------------------------------------------- |
| `license-classifications.yml` + `evaluator.rules.kts` | Policy                                    | Every package with that license, org-wide                       |
| `curations.yml`                                       | Package metadata                          | One specific package (often one version)                        |
| `resolutions.yml`                                     | Nothing — documents an accepted exception | One specific flagged violation, on one specific package/version |

## Category taxonomy

The actual categories (`permissive`, `permissive-emerging`,
`attribution-content`, etc.) and the reasoning behind each are documented
inline in `license-classifications.yml` itself — treated as the single
source of truth rather than restated here, to avoid the taxonomy drifting
out of sync between two places that describe it. If you're trying to
understand why a specific license was categorized a certain way, read the
comments in that file directly.
