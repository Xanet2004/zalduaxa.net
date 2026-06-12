# Security Scanning

Trivy repository/filesystem security scanning for vulnerabilities, secrets, and misconfigurations.

[Back to menu](/README.md)

---

## What Trivy scans

Trivy analyzes the repository filesystem for three categories:

| Scanner | Targets |
|---------|---------|
| Vulnerabilities (`vuln`) | Maven dependencies (`pom.xml`), npm dependencies (`package.json`, `package-lock.json`) |
| Secrets (`secret`) | Hardcoded credentials, tokens, API keys in tracked files |
| Misconfigurations (`misconfig`) | Dockerfiles, Docker Compose files, GitHub Actions workflows |

---

## Continuous Integration workflow

**File**: `.github/workflows/security.yml`

The workflow runs automatically on:

- Every push and pull request.
- Weekly schedule (Monday 06:00 UTC).
- Manual trigger via GitHub Actions UI.

### Steps

1. Generate a SARIF report scanning for HIGH and CRITICAL issues.
2. Upload the SARIF report to GitHub Code Scanning (available in the Security tab).
3. Run a separate enforcement gate that fails the workflow only on CRITICAL findings.

SARIF upload is non-blocking — if upload fails (e.g., on fork PRs), the workflow still reports results in logs.

### Scan scope

| What | Why |
|------|-----|
| `backend/pom.xml` | Maven dependency vulnerabilities |
| `frontend/package.json` | npm dependency manifest |
| `frontend/package-lock.json` | npm lockfile with precise transitive dependencies |
| `backend/Dockerfile` | Dockerfile misconfigurations and OS package vulnerabilities |
| `frontend/Dockerfile` | Dockerfile misconfigurations |
| `docker-compose*.yml` | Compose file misconfigurations |
| `.github/workflows/*.yml` | GitHub Actions workflow misconfigurations |

### Skipped directories

Generated and runtime directories are excluded to reduce noise:

```text
backend/target
frontend/node_modules
frontend/dist
.scannerwork
storage
.git
```

### Severity policy

| Level | Action |
|-------|--------|
| CRITICAL | Fails the workflow |
| HIGH | Reported in SARIF and logs, does not fail |

HIGH blocking may be enabled later after the initial scan results have been reviewed and addressed.

---

## Local Trivy command

No local install required — run Trivy via Docker:

```bash
docker run --rm \
  -v "$(pwd):/project" \
  aquasec/trivy:latest fs \
  --scanners vuln,secret,misconfig \
  --severity HIGH,CRITICAL \
  --ignore-unfixed \
  --skip-dirs /project/backend/target \
  --skip-dirs /project/frontend/node_modules \
  --skip-dirs /project/frontend/dist \
  --skip-dirs /project/.scannerwork \
  --skip-dirs /project/storage \
  --skip-dirs /project/.git \
  /project
```

Remove `--ignore-unfixed` to also see vulnerabilities that have no available fix yet:

```bash
docker run --rm \
  -v "$(pwd):/project" \
  aquasec/trivy:latest fs \
  --scanners vuln,secret,misconfig \
  --severity HIGH,CRITICAL \
  --skip-dirs /project/backend/target \
  --skip-dirs /project/frontend/node_modules \
  --skip-dirs /project/frontend/dist \
  --skip-dirs /project/.scannerwork \
  --skip-dirs /project/storage \
  --skip-dirs /project/.git \
  /project
```

---

## Output

The CI workflow generates `trivy-results.sarif`, which is uploaded to GitHub Code Scanning when permissions are available. Results appear under the repository's **Security → Code Scanning** tab.

---

## Suppressing findings

Do not create `.trivyignore` unless a confirmed false positive or accepted risk needs to be documented. Create it only after verifying the finding is not exploitable in this project's context.

---

## Postponed tasks

These are intentionally out of scope for the current implementation:

- **Docker image scanning** — will be added after repository scanning is stable and images are already built in CI.
- **Blocking on HIGH severity** — will be reconsidered after initial scan results are reviewed.
- **`.trivyignore`** — should only be created when a real false positive is found.
- **`trivy.yaml`** — configuration file may be added later if options grow beyond what fits in the workflow.
- **Deployment security gates** — runtime security monitoring is a separate phase.

---

## Relationship to Dependabot

Dependabot tracks **direct dependency updates** for Maven, npm, and GitHub Actions. Trivy complements this by detecting:

- **Transitive dependency vulnerabilities** that Dependabot may not flag.
- **OS-level packages** inside Docker base images (once image scanning is added).
- **Secrets and misconfigurations** that Dependabot does not check.
- **Lockfile inconsistencies** between the manifest and lockfile.
