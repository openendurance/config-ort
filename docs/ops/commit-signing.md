# Commit Signing (DCO + Cryptographic Signatures)

## Why this exists

OEI repositories enforce two independent, unrelated checks on every commit:
a **DCO sign-off** and a **cryptographic signature**. Their git flags look
almost identical (`-s` vs `-S`), which makes them easy to conflate — but
satisfying one does nothing for the other. This doc exists because that
confusion cost real troubleshooting time during `config-ort`'s bootstrap,
and will cost it again for anyone who hits it cold.

## The two mechanisms

|                     | DCO Sign-off                                                           | Cryptographic Signature                              |
| ------------------- | ---------------------------------------------------------------------- | ---------------------------------------------------- |
| **Proves**          | You have the right to contribute this code under the project's license | This commit genuinely came from you, unaltered       |
| **Mechanism**       | A `Signed-off-by:` trailer in the commit message (plain text)          | A GPG or SSH signature embedded in the commit object |
| **Flag**            | `git commit -s`                                                        | `git commit -S`                                      |
| **Enforced by**     | The DCO GitHub App, as a required status check                         | The repo's branch ruleset ("Require signatures")     |
| **Checked against** | Nothing — it's just text in the message                                | A signing key registered to your GitHub account      |

Both at once: `git commit -S -s`, or set `commit.gpgsign true` globally and
always pass `-s` explicitly (sign-off is never automatic, even with
`gpgsign` on).

## One-time setup: cryptographic signing

SSH-based signing is simpler than GPG if you don't already have a GPG key
you use for this.

1. Generate a key if you don't already have one you want to use:

```sh
   ssh-keygen -t ed25519 -C "your-email@example.com" -f ~/.ssh/git_signing_key
```

2. On GitHub: **Settings → SSH and GPG keys → New SSH key** — set **Key
   type: Signing Key**, not Authentication Key. This is the single most
   common failure point: an SSH key already added for authentication
   (cloning, pushing) is a **separate registration** from one GitHub trusts
   for signature verification, even when it's the literal same key file.
   Adding it once as an auth key does not cover this — it has to be added
   again, explicitly as a signing key.
3. Configure git to use it:

```sh
   git config --global gpg.format ssh
   git config --global user.signingkey ~/.ssh/git_signing_key.pub
   git config --global commit.gpgsign true
```

4. (Optional, for local verification only — see below) Set up an allowed
   signers file so `git log --show-signature` can actually check things
   locally instead of erroring:

```sh
   mkdir -p ~/.config/git
   echo "your-email@example.com $(cat ~/.ssh/git_signing_key.pub)" >> ~/.config/git/allowed_signers
   git config --global gpg.ssh.allowedSignersFile ~/.config/git/allowed_signers
```

Step 4 is **not required** for GitHub to accept your signed commits —
GitHub verifies independently against the keys registered on your account.
It only affects whether your own local `git log` can confirm a signature.
Skipping it will _not_ cause a push rejection; it will cause a confusing
local error (see Troubleshooting below).

## Fixing commits that are missing one or both

If you've already committed without sign-off and/or signature, the fix is a
rebase, not individual amends — rewrite the range, then force-push.

**Add DCO sign-off to existing commits:**

```sh
git rebase --signoff --gpg-sign origin/develop
git push --force-with-lease origin <branch>
```

`--signoff` during a rebase is idempotent — commits that already have a
correct `Signed-off-by:` trailer are left unchanged, so it's safe to run
across a whole branch rather than needing to isolate just the
non-compliant commits.

**Add cryptographic signatures to existing commits** (safe to run even on
commits that already have correct sign-off — this only touches the
signature):

```sh
git rebase --root --exec "git commit --amend --no-edit -S"
git push --force-with-lease origin <branch>
```

Both can be combined in one amend if doing both at once from scratch:
`git commit --amend --no-edit -S -s`.

**Before rewriting history on a shared or already-merged branch**, confirm
nobody else has work based on the commits you're about to rewrite, and that
nothing (a release tag, a published package's provenance attestation) is
already pointing at the specific commit SHAs you're changing. A `--root`
rebase touches everything — fine on a repo you're the sole author of and
haven't tagged/released yet; a materially bigger decision once either of
those is true.

## Troubleshooting

**`git log --show-signature` says "No signature" but you're sure you
signed it.**
Check the raw commit object before trusting that message:

```sh
git cat-file commit HEAD | head -5
```

If you see a `gpgsig` line, the commit **is** signed — the "No signature"
output is a local-verification-only failure, usually because
`gpg.ssh.allowedSignersFile` isn't configured (see setup step 4). This does
**not** mean GitHub will reject the push — GitHub verifies independently.
Don't assume a push will fail based on this message alone; the only way to
know for certain is to actually push.

**Push rejected: "Commits must have verified signatures."**
First confirm this is actually the signature check and not the DCO check —
they produce differently-worded, differently-named failures (`DCO / DCO` vs
a ruleset violation naming "verified signatures" specifically). If it
genuinely is the signature check:

1. Confirm local config is correct: `git config --get commit.gpgsign`,
   `git config --get gpg.format`, `git config --get user.signingkey` should
   all return values, and the signing key file should actually exist at
   that path.
2. Confirm via `git cat-file` (above) that commits actually contain a
   `gpgsig` line.
3. If both check out, the remaining likely cause is the key being
   registered on GitHub as an Authentication key only, not a Signing key —
   see setup step 2. Re-add the same key with the correct type; no need to
   regenerate or re-sign anything once that's fixed.

**Push rejected: `DCO / DCO` check, listing specific commit hashes.**
This is the sign-off check, unrelated to signatures. Those commits are
missing a `Signed-off-by:` trailer. Fix with the `--signoff` rebase above —
`git commit -s` does **not** need to be combined with any signature flag to
satisfy this specific check, though both checks are independently required
to actually merge.

## Common mistake

Running `git commit -s` when a push is rejected for "Commits must have
verified signatures" — this fixes nothing, since `-s` only ever touches DCO
sign-off. If a push is failing, read which check is actually failing (the
check name and message differ meaningfully between the two) before picking
a flag.
