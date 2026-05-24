# Git and Versioning

## Branching Flow

- Work happens on feature branches.
- Pull requests are opened against `main`.
- CI runs on pull requests to validate the backend before merge.
- Releases are created from `main` after merge.

## Versioning Rule

The project uses semantic versioning:

- `MAJOR.MINOR.PATCH`

Suggested meaning:

- `MAJOR`: breaking changes
- `MINOR`: backward-compatible feature additions
- `PATCH`: backward-compatible bug fixes

## Tagging Convention

Release tags should use a backend prefix:

- `b-0.0.1`
- `b-0.1.0`
- `b-1.0.0`

Later, the frontend can use the same pattern with an `f-` prefix.

## When To Tag

Tags should be created after merge on `main`, not on the pull request branch.

The tag must point to the merge commit head, meaning the exact commit that landed on `main`.

This keeps the release history aligned with what is actually deployed.

## Changelog

You do not need to maintain a manual changelog before every merge if you use GitHub releases.

Recommended workflow:

- PRs focus on code review and CI validation.
- The merge commit on `main` is tagged.
- The tag triggers a release with generated notes.

If you later want a manual `CHANGELOG.md`, update it in the PR before merge so the release stays aligned with what actually shipped.

## Current CI Scope

For now, the backend CI only:

- triggers on pull requests that touch backend-related files
- runs build and tests
- does not create tags
- does not publish Docker images yet
