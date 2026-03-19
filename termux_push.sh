#!/data/data/com.termux/files/usr/bin/bash
# SentinelOS — Termux push script
# Usage: bash termux_push.sh "your commit message"

set -e

MSG="${1:-update SentinelOS}"
REPO_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "══════════════════════════════════════"
echo "  SentinelOS — Termux Push Script"
echo "══════════════════════════════════════"
echo "Repo: $REPO_DIR"
echo "Message: $MSG"
echo ""

cd "$REPO_DIR"

# Stage all changes
git add -A

# Show what will be committed
echo "─── Changes to commit ───"
git status --short
echo ""

# Commit
git commit -m "$MSG" || echo "(nothing to commit)"

# Push
echo "─── Pushing to GitHub ───"
git push

echo ""
echo "✅ Push complete! GitHub Actions will now build the APK."
echo "   Monitor: https://github.com/mukhteghwagae-gif/SentinelOs/actions"
