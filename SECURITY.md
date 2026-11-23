# Security Policy

## Supported Versions

| Version | Supported |
| ------- | --------- |
| 1.0.x   | Yes       |

## Reporting a Vulnerability

If you discover a security vulnerability in **EQ-Math** (such as a Firebase Firestore rule bypass, authentication issue, or leaked credential):

1. **Do not** open a public GitHub issue with exploit details.
2. Use GitHub's **Private Vulnerability Reporting** ("Report a vulnerability" under the **Security** tab), or contact the maintainers directly.
3. Include a clear description, reproduction steps, and potential impact.

We aim to acknowledge security reports within 72 hours and coordinate a fix promptly.

## Secret & Data Safety Guidelines

- Never commit `.env`, `my-upload-key.jks`, or private API credentials to git.
- All Firestore collections follow strict per-user `request.auth.uid` ownership rules enforced in `firestore.rules` and verified by `firestore.test.js`.
- Guest Mode (`LocalGuestMathRepository`) stores session progress strictly in-memory without touching remote Firestore documents.
