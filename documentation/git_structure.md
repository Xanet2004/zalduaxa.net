# Git Branch Structure
[Back to menu](/README.md)

- **main**: Production-ready state. Only stable and tested code should be merged here.  
- **develop**: Main development branch. All new features and changes should be merged here first.  
- **hotfix**: For urgent bug fixes that need to be applied immediately to production.  

---

## Merging without fast-forward

When there are no changes in the main branch and you merge develop into it, Git may automatically perform a fast-forward merge. However, this reduces the readability of the Git Graph.

To prevent this and force a merge commit, use the following commands:

```
git checkout main
git pull origin main

git merge --no-ff develop

git push origin main
```

## Release and Versioning (main)

The goal is to keep `main` clean and always deployable. Every stable version in `main` should be marked with a **version tag** following Semantic Versioning.

### Version naming (SemVer)

Use the format:

```
vMAJOR.MINOR.PATCH
```

Examples:
- `v0.2.0` new features added (backwards compatible)
- `v0.2.1` bugfix release
- `v1.0.0` first stable major release

Pre-releases (optional):
- `v0.3.0-rc.1` release candidate
- `v0.3.0-beta.1` beta

### When to create a release tag

Create a new version tag when:
- `develop` has been merged into `main`, and
- the result is stable (builds, runs, and basic flows are working).

This means `main` becomes a list of stable milestones, and tags are the official release markers.

### Creating a release (recommended workflow)

1. Merge `develop` into `main` (via PR if possible)
2. Create an annotated tag on the merge commit in `main`
3. Push the tag

Commands:

```bash
git checkout main
git pull

git tag -a v0.2.0 -m "Release v0.2.0"
git push origin main --tags
```

### Release notes (changelog)

After pushing the tag, create a release page for that tag in GitHub and include:
- summary of changes
- highlights (new features)
- breaking changes (if any)
- migration notes (DB changes, config changes)

Optional (recommended): keep a `CHANGELOG.md` in the repo and update it on every release.

---

## Hotfix workflow (urgent production fixes)

Use this when `main` is broken and you need a quick fix without waiting for ongoing `develop` changes.

1. Create a branch from `main`
2. Apply the fix
3. Merge back to `main`
4. Tag a PATCH version
5. Merge the hotfix back into `develop` (so it doesn’t get lost)

Example:

```bash
git checkout main
git pull
git checkout -b hotfix/0.2.1

# commit your fix here
git commit -m "fix(backend): ..."

git checkout main
git merge --no-ff hotfix/0.2.1
git tag -a v0.2.1 -m "Hotfix v0.2.1"
git push origin main --tags

git checkout develop
git merge --no-ff hotfix/0.2.1
git push origin develop
```

---

## Release branches (optional, for bigger releases)

If you want a short stabilization phase before tagging a release:

- Create `release/x.y.z` from `develop`
- Only allow final fixes, docs, version bumps
- Merge `release/x.y.z` into `main` and tag
- Merge back into `develop`

Naming:
- `release/0.3.0`

---

## Git Commit Syntax

To keep commits clean and understandable, use the following syntax:

```
<type>(<scope>): <short description>
```

### Commit Types

- **feat**: A new feature
- **fix**: A bug fix
- **clean**: Cleaning the code, no functionality changes but more interpretable code
- **docs**: Documentation changes
- **style**: Code style changes (formatting, missing semicolons, etc.)
- **refactor**: Code changes that neither fix a bug nor add a feature
- **test**: Adding or updating tests
- **chore**: Changes to the build process or auxiliary tools

### Examples

```bash
git commit -m "feat(frontend): add responsive navbar"
git commit -m "fix(backend): correct user authentication error"
git commit -m "docs: update README with project structure"
git commit -m "style: fix indentation in main.css"
```
