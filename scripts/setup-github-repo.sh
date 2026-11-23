#!/usr/bin/env bash
# ==============================================================================
# EQ-Math GitHub Repository Setup Script
# Syncs repository description, GitHub Pages homepage, 20 topics/tags,
# enables GitHub Pages (GitHub Actions), and tags v1.0.0 release.
#
# Prerequisites:
#   - GitHub CLI (`gh`) installed and authenticated (`gh auth login`)
#   - Run from the repository root inside a cloned GitHub repo
# ==============================================================================
set -euo pipefail

META_FILE=".github/repo-metadata.json"

if ! command -v gh >/dev/null 2>&1; then
  echo "Error: GitHub CLI (gh) is not installed. Install from https://cli.github.com/"
  exit 1
fi

if ! command -v python3 >/dev/null 2>&1; then
  echo "Error: python3 is required to parse ${META_FILE}."
  exit 1
fi

REPO_NWO="${1:-$(gh repo view --json nameWithOwner -q .nameWithOwner)}"
OWNER="${REPO_NWO%%/*}"
REPO="${REPO_NWO##*/}"
PAGES_URL="https://${OWNER}.github.io/${REPO}/"

DESCRIPTION="$(python3 -c "import json; print(json.load(open('${META_FILE}'))['description'])")"
TOPICS_JSON="$(python3 -c "import json; print(json.dumps({'names': json.load(open('${META_FILE}'))['topics']}))")"
RELEASE_TAG="$(python3 -c "import json; print(json.load(open('${META_FILE}'))['default_release_tag'])")"

echo "Configuring repository: ${REPO_NWO}"
echo "  Description : ${DESCRIPTION}"
echo "  Homepage    : ${PAGES_URL}"

gh repo edit "${REPO_NWO}" \
  --description "${DESCRIPTION}" \
  --homepage "${PAGES_URL}" \
  --enable-issues \
  --enable-wiki=false

echo "Applying 20 GitHub topics/tags..."
printf '%s' "${TOPICS_JSON}" | gh api \
  --method PUT \
  -H "Accept: application/vnd.github+json" \
  "repos/${REPO_NWO}/topics" \
  --input - >/dev/null

echo "Enabling GitHub Pages (GitHub Actions build source)..."
gh api \
  --method POST \
  -H "Accept: application/vnd.github+json" \
  "repos/${REPO_NWO}/pages" \
  -f build_type="workflow" >/dev/null 2>&1 || \
gh api \
  --method PUT \
  -H "Accept: application/vnd.github+json" \
  "repos/${REPO_NWO}/pages" \
  -f build_type="workflow" >/dev/null 2>&1 || \
echo "  Note: Enable Pages manually under Settings -> Pages -> Source: GitHub Actions."

if git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  if ! git rev-parse "${RELEASE_TAG}" >/dev/null 2>&1; then
    echo "Creating annotated git tag ${RELEASE_TAG}..."
    git tag -a "${RELEASE_TAG}" -m "Release ${RELEASE_TAG} — EQ-Math Belajar Jadi Mudah!"
    echo "Push tag to trigger Release CI/CD: git push origin ${RELEASE_TAG}"
  else
    echo "Tag ${RELEASE_TAG} already exists locally."
  fi
fi

echo "Done! Repository metadata, topics, and Pages configuration synced."
