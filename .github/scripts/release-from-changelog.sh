#!/usr/bin/env bash
# Prepares a release from the [Unreleased] section in CHANGELOG.md.
# Writes outputs for GitHub Actions and updates CHANGELOG.md in the working tree.
set -euo pipefail

CHANGELOG="${CHANGELOG_PATH:-CHANGELOG.md}"
BUMP="${VERSION_BUMP:-patch}"
GITHUB_OUTPUT="${GITHUB_OUTPUT:?GITHUB_OUTPUT must be set}"

write_output() {
  while IFS='=' read -r key value; do
    echo "${key}=${value}" >> "$GITHUB_OUTPUT"
  done
}

if [[ ! -f "$CHANGELOG" ]]; then
  write_output <<EOF
should_release=false
skip_reason=CHANGELOG.md not found
EOF
  exit 0
fi

mapfile -t existing_tags < <(git tag -l 'v[0-9]*.[0-9]*.[0-9]*' 2>/dev/null | sort -V -r || true)
latest_tag="${existing_tags[0]:-}"

if [[ -z "$latest_tag" ]]; then
  major=1
  minor=0
  patch=0
else
  version="${latest_tag#v}"
  IFS='.' read -r major minor patch <<< "$version"
  major="${major:-0}"
  minor="${minor:-0}"
  patch="${patch:-0}"
  case "$BUMP" in
    major)
      major=$((major + 1))
      minor=0
      patch=0
      ;;
    minor)
      minor=$((minor + 1))
      patch=0
      ;;
    patch)
      patch=$((patch + 1))
      ;;
    *)
      write_output <<EOF
should_release=false
skip_reason=Unknown VERSION_BUMP '${BUMP}' (use patch, minor, or major)
EOF
      exit 0
      ;;
  esac
fi

NEW_VERSION="${major}.${minor}.${patch}"
TAG="v${NEW_VERSION}"

if git rev-parse "$TAG" >/dev/null 2>&1; then
  write_output <<EOF
should_release=false
skip_reason=Tag ${TAG} already exists
EOF
  exit 0
fi

unreleased_content="$(awk '
  /^## \[Unreleased\]/ { capture = 1; next }
  capture && /^## \[/ { exit }
  capture { print }
' "$CHANGELOG")"

if ! printf '%s\n' "$unreleased_content" | grep -qE '^[[:space:]]*-[[:space:]]+'; then
  write_output <<EOF
should_release=false
skip_reason=No bullet points under [Unreleased] in CHANGELOG.md (add lines like "- Your change")
EOF
  exit 0
fi

release_date="$(date -u +%Y-%m-%d)"
release_notes_path="${RUNNER_TEMP:-/tmp}/cauldrontap-release-notes.md"

{
  echo "## CauldronTap ${NEW_VERSION} (${release_date})"
  echo ""
  printf '%s\n' "$unreleased_content"
} > "$release_notes_path"

header_and_unreleased="$(awk '
  /^## \[Unreleased\]/ { print; exit }
  { print }
' "$CHANGELOG")"

remaining_sections="$(awk '
  /^## \[Unreleased\]/ { skip = 1; next }
  skip && /^## \[/ { skip = 0 }
  skip { next }
  { print }
' "$CHANGELOG")"

unreleased_template="$(cat <<'EOF'

### Added

### Changed

### Fixed

EOF
)"

new_version_section="$(cat <<EOF
## [${NEW_VERSION}] - ${release_date}
${unreleased_content}
EOF
)"

{
  printf '%s\n' "$header_and_unreleased"
  printf '%s\n' "$unreleased_template"
  echo ""
  printf '%s\n' "$new_version_section"
  if [[ -n "$remaining_sections" ]]; then
    echo ""
    printf '%s\n' "$remaining_sections"
  fi
} > "${CHANGELOG}.tmp"

mv "${CHANGELOG}.tmp" "$CHANGELOG"

write_output <<EOF
should_release=true
version=${NEW_VERSION}
tag=${TAG}
release_notes_path=${release_notes_path}
EOF

echo "Prepared release ${TAG}"
echo "Release notes written to ${release_notes_path}"
