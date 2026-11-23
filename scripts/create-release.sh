#!/usr/bin/env bash
# ==============================================================================
# EQ-Math APK Tagging & Release Helper
#
# Usage:
#   ./scripts/create-release.sh                # Tags current versionName (e.g. v1.0.0) & pushes tag
#   ./scripts/create-release.sh 1.1.0          # Bumps versionName & versionCode, commits, tags v1.1.0
#   ./scripts/create-release.sh 1.1.0 --local  # Also builds APK locally and publishes via `gh release create`
# ==============================================================================
set -euo pipefail

GRADLE_FILE="app/build.gradle.kts"
NEW_VERSION="${1:-}"
MODE="${2:-ci}"

if ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  echo "Error: Run this script inside a git repository cloned from GitHub."
  exit 1
fi

CURRENT_VERSION="$(grep -E 'versionName\s*=' "${GRADLE_FILE}" | head -n1 | sed -E 's/.*"([^"]+)".*/\1/')"
CURRENT_CODE="$(grep -E 'versionCode\s*=' "${GRADLE_FILE}" | head -n1 | sed -E 's/[^0-9]*([0-9]+).*/\1/')"

if [ -n "${NEW_VERSION}" ] && [ "${NEW_VERSION}" != "--local" ]; then
  CLEAN_VERSION="${NEW_VERSION#v}"
  NEXT_CODE=$((CURRENT_CODE + 1))
  echo "Bumping Android version: ${CURRENT_VERSION} (${CURRENT_CODE}) -> ${CLEAN_VERSION} (${NEXT_CODE})"

  python3 - "${GRADLE_FILE}" "${CLEAN_VERSION}" "${NEXT_CODE}" <<'PY'
import re, sys
path, ver, code = sys.argv[1], sys.argv[2], sys.argv[3]
text = open(path, encoding="utf-8").read()
text = re.sub(r'versionCode\s*=\s*\d+', f'versionCode = {code}', text, count=1)
text = re.sub(r'versionName\s*=\s*"[^"]+"', f'versionName = "{ver}"', text, count=1)
open(path, "w", encoding="utf-8").write(text)
PY

  git add "${GRADLE_FILE}"
  git commit -m "chore(release): bump version to v${CLEAN_VERSION} (versionCode ${NEXT_CODE})"
  TARGET_VERSION="${CLEAN_VERSION}"
else
  if [ "${NEW_VERSION}" = "--local" ]; then
    MODE="--local"
  fi
  TARGET_VERSION="${CURRENT_VERSION}"
fi

TAG="v${TARGET_VERSION}"

if git rev-parse -q --verify "refs/tags/${TAG}" >/dev/null; then
  echo "Tag ${TAG} already exists locally."
else
  echo "Creating annotated git tag ${TAG}..."
  git tag -a "${TAG}" -m "Release ${TAG} — EQ-Math Belajar Jadi Mudah!"
fi

echo "Pushing branch and tag ${TAG} to origin..."
git push origin HEAD
git push origin "${TAG}"

if [ "${MODE}" = "--local" ]; then
  echo "Building local APK & publishing GitHub Release via gh CLI..."
  if [ ! -f debug.keystore ] && [ -f debug.keystore.base64 ]; then
    base64 --decode debug.keystore.base64 > debug.keystore
  fi
  if [ ! -f .env ] && [ -f .env.example ]; then
    cp .env.example .env
  fi

  gradle :app:assembleDebug
  mkdir -p release-dist
  cp app/build/outputs/apk/debug/app-debug.apk "release-dist/EQ-Math-${TAG}-debug.apk"
  (cd release-dist && sha256sum * > SHA256SUMS.txt)

  gh release create "${TAG}" \
    "release-dist/EQ-Math-${TAG}-debug.apk" \
    "release-dist/SHA256SUMS.txt" \
    --title "EQ-Math ${TAG}" \
    --generate-notes \
    --latest
  echo "Published GitHub Release ${TAG}!"
else
  echo "Tag ${TAG} pushed! GitHub Actions ('Tag & Release APK') will now build the APK and publish Release ${TAG}."
fi
