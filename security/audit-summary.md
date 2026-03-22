# 🔒 Security Audit — Inventory Management System

**Date:** 2026-03-22  
**Scanner:** SecurityScanner v1  
**Project:** ~/projects/inventory-system/

---

## Scan Results

| Tool | Status | Issues |
|------|--------|--------|
| Semgrep Android | ⚠️ | 1 |
| Semgrep Backend | ⚠️ | 1 |
| Bandit | ✅ | 0 |
| pip-audit | ❌ | 7 CVEs (4 packages) |
| Trufflehog | ❌ | Not available (Docker not found) |
| Android Deps CVE (Gradle) | ❌ | Plugin not configured |

---

## Issues Found

### CRITICAL
_None detected._

### HIGH

#### [pip-audit] python-jose 3.3.0 — Algorithm Confusion (PYSEC-2024-232)
- **Package:** `python-jose==3.3.0`
- **CVE:** PYSEC-2024-232
- **Description:** Algorithm confusion with OpenSSH ECDSA keys and other key formats — allows JWT forgery/bypass.
- **Fix:** Upgrade to `python-jose>=3.4.0`

#### [pip-audit] python-jose 3.3.0 — DoS (PYSEC-2024-233)
- **Package:** `python-jose==3.3.0`
- **CVE:** PYSEC-2024-233
- **Description:** Attackers can cause denial of service via resource exhaustion during JWT parsing.
- **Fix:** Upgrade to `python-jose>=3.4.0`

#### [pip-audit] python-multipart 0.0.9 — Path Traversal (GHSA-wp53-j4wj-2cfg)
- **Package:** `python-multipart==0.0.9`
- **CVE:** GHSA-wp53-j4wj-2cfg
- **Description:** Path traversal vulnerability in file upload handling with non-default config.
- **Fix:** Upgrade to `python-multipart>=0.0.22`

#### [pip-audit] ecdsa 0.19.1 — Minerva Timing Attack (GHSA-wj6h-64fc-37mp)
- **Package:** `ecdsa==0.19.1`
- **CVE:** GHSA-wj6h-64fc-37mp
- **Description:** Vulnerable to Minerva timing attack on P-256 curve — could leak private key material.
- **Fix:** No fix version available; consider replacing with `cryptography` library.

### MEDIUM

#### [Semgrep Android] Exported Activity — CWE-926 (AndroidManifest.xml:16)
- **File:** `app/src/main/AndroidManifest.xml` line 16
- **Rule:** `java.android.security.exported_activity`
- **Description:** An activity is exported without restriction. Any app on the device can launch it, potentially compromising app integrity or data.
- **CWE:** CWE-926 — Improper Export of Android Application Components
- **OWASP:** A5:2021 Security Misconfiguration
- **Fix:** Add `android:exported="false"` or add explicit `<intent-filter>` protection and validate callers.

#### [pip-audit] python-multipart 0.0.9 — Form Field Injection (GHSA-59g5-xgcq-4qw3)
- **Package:** `python-multipart==0.0.9`
- **CVE:** GHSA-59g5-xgcq-4qw3
- **Description:** Parser skips CR/LF in field names allowing potential form data injection.
- **Fix:** Upgrade to `python-multipart>=0.0.18`

#### [pip-audit] starlette 0.37.2 — Form Data DoS (GHSA-f96h-pmfr-66vw)
- **Package:** `starlette==0.37.2`
- **CVE:** GHSA-f96h-pmfr-66vw
- **Description:** multipart/form-data parts without filename treated as text fields — can exhaust memory.
- **Fix:** Upgrade to `starlette>=0.40.0`

#### [pip-audit] starlette 0.37.2 — Large File Upload DoS (GHSA-2c2j-9gv5-cj73)
- **Package:** `starlette==0.37.2`
- **CVE:** GHSA-2c2j-9gv5-cj73
- **Description:** Multi-part form with large files can exceed spool size limit causing DoS.
- **Fix:** Upgrade to `starlette>=0.47.2`

### LOW

#### [Semgrep Backend] Wildcard CORS — CWE-942 (app/main.py:23)
- **File:** `app/main.py` line 23
- **Rule:** `python.fastapi.security.wildcard-cors`
- **Description:** CORS policy uses wildcard `*`, allowing any origin to make cross-domain requests.
- **CWE:** CWE-942 — Permissive Cross-domain Policy
- **OWASP:** A05:2021 Security Misconfiguration
- **Fix:** Replace `allow_origins=["*"]` with an explicit allowlist of trusted origins.

---

## Remediation Priority

| Priority | Action |
|----------|--------|
| 🔴 Immediate | Upgrade `python-jose` to `>=3.4.0` (JWT auth bypass + DoS) |
| 🔴 Immediate | Upgrade `python-multipart` to `>=0.0.22` (path traversal) |
| 🟠 Soon | Upgrade `starlette` to `>=0.47.2` (DoS via form uploads) |
| 🟠 Soon | Fix exported Android activity in `AndroidManifest.xml` |
| 🟡 Eventually | Restrict CORS in `app/main.py` to known origins |
| 🟡 Eventually | Replace `ecdsa` with `cryptography` package |
| ⬜ Deferred | Configure OWASP Dependency-Check Gradle plugin for Android deps |
| ⬜ Deferred | Set up Trufflehog for secrets scanning in CI |

---

## Scans Not Completed

| Scan | Reason |
|------|--------|
| Trufflehog secrets detection | Docker not available in environment |
| Android dependency CVE check | OWASP `dependencyCheckAnalyze` task not configured in `build.gradle` |
| Semgrep `p/android` ruleset | HTTP 404 — ruleset unavailable; used `p/kotlin` + `p/owasp-top-ten` instead |

---

## Ready for Delivery: ❌ NO

**Blocking issues:** 4 HIGH severity CVEs in Python dependencies must be patched before release.  
**Recommended:** Upgrade vulnerable packages, re-run `pip-audit` to verify clean, then re-assess.
