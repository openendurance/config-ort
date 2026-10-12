# Release Workflow Settings

This runbook documents the required GitHub Actions settings for the Changesets-based release flow in this repository.

## Why this exists

The release workflow needs permission to:

- create or update a release pull request,
- push release branch and tags,
- publish to npm,
- create GitHub Releases.

If these permissions are blocked at enterprise or organization policy scope, the workflow can fail even when the repository workflow YAML is correct.

## Required GitHub Actions permissions

The most restrictive policy wins: Enterprise → Organization → Repository.

1. Enterprise policy (if org is enterprise-managed)
    - Allow Actions workflow token write access (not read-only enforcement).
    - Allow GitHub Actions to create and approve pull requests.
2. Organization settings
    - Actions → General → Workflow permissions: Read and write permissions.
    - Enable Allow GitHub Actions to create and approve pull requests.
    - If repository-level overrides are needed, allow repositories to use workflow permissions.
3. Repository settings
    - Actions → General → Workflow permissions: Read and write permissions.
    - Enable Allow GitHub Actions to create and approve pull requests.

## Required workflow-level permissions

The release workflow should keep job permissions that include:

- contents: write
- pull-requests: write
- id-token: write

Reference: [.github/workflows/release.yml](../../.github/workflows/release.yml)

## Expected successful outcomes

After merging a Changesets release PR into main and running release:

1. Package is published to npm.
2. Version tag(s) are created in GitHub for released package version(s).
3. A GitHub Release entry is created for the published version(s).

If release fails with a pull request permission error, re-check enterprise/org Actions policy first.
