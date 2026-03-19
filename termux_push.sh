#!/data/data/com.termux/files/usr/bin/bash
# ============================================================
#  SentinelOS — Termux Setup & GitHub Push Script
#  cd /path/to/SentinelOS && bash termux_push.sh
# ============================================================

set -e
RED='\033[0;31m'; GREEN='\033[0;32m'; CYAN='\033[0;36m'; YELLOW='\033[1;33m'; NC='\033[0m'
info()    { echo -e "${CYAN}[INFO]${NC}   $1"; }
success() { echo -e "${GREEN}[OK]${NC}     $1"; }
warn()    { echo -e "${YELLOW}[WARN]${NC}   $1"; }
fail()    { echo -e "${RED}[FAIL]${NC}   $1"; exit 1; }

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
GITHUB_USER="mukhteghwagae-gif"
REPO_NAME="SentinelOS"
BRANCH="main"

cd "$PROJECT_DIR"
info "Project root: $PROJECT_DIR"

# ── Step 1: Termux packages ───────────────────────────────────────────────────
info "Installing packages (git, openjdk-17)..."
pkg install -y git openjdk-17 2>/dev/null || true
success "Packages ready"

# ── Step 2: Generate gradle-wrapper.jar ──────────────────────────────────────
WRAPPER_JAR="gradle/wrapper/gradle-wrapper.jar"
mkdir -p gradle/wrapper

if [ -s "$WRAPPER_JAR" ]; then
    success "gradle-wrapper.jar present ($(du -sh $WRAPPER_JAR | cut -f1))"
else
    info "gradle-wrapper.jar missing — fetching..."

    if command -v gradle &>/dev/null; then
        gradle wrapper --gradle-version 8.4 --distribution-type bin
        success "Wrapper generated via gradle"
    elif command -v curl &>/dev/null; then
        curl -fsSL \
            "https://github.com/gradle/gradle/raw/v8.4.0/gradle/wrapper/gradle-wrapper.jar" \
            -o "$WRAPPER_JAR"
        JAR_SIZE=$(wc -c < "$WRAPPER_JAR")
        [ "$JAR_SIZE" -lt 10000 ] && fail "Download failed (size: ${JAR_SIZE}B) — check internet"
        success "Wrapper JAR downloaded ($(du -sh $WRAPPER_JAR | cut -f1))"
    else
        warn "Install gradle first: pkg install gradle"
        fail "Cannot generate gradle-wrapper.jar"
    fi
fi

chmod +x gradlew

# ── Step 3: Git setup ─────────────────────────────────────────────────────────
[ ! -d ".git" ] && git init && git branch -M "$BRANCH" && success "Git initialised"

if git remote get-url origin &>/dev/null; then
    git remote set-url origin "https://github.com/${GITHUB_USER}/${REPO_NAME}.git"
else
    git remote add origin "https://github.com/${GITHUB_USER}/${REPO_NAME}.git"
fi

[ -z "$(git config user.email)" ] && git config user.email "sentinel@sentinelos.app"
[ -z "$(git config user.name)"  ] && git config user.name  "SentinelOS Build"

# ── Step 4: Commit ────────────────────────────────────────────────────────────
git add -A
if git diff --cached --quiet; then
    info "Nothing to commit — already up to date"
else
    git commit -m "fix: GradleWrapperMain + full SentinelOS corrected build

- Workflow downloads gradle-wrapper.jar via curl (fixes ClassNotFoundException)
- Android 12+ BLE permissions added (BLUETOOTH_SCAN/CONNECT)
- All Services declared in Manifest with foregroundServiceType
- NotificationChannels created in Application.onCreate()
- All R.id mismatches fixed (100% verified Kotlin vs XML)
- NightGuardService: real accelerometer motion detection loop
- MagnetometerManager: 6-sensor fusion + anomaly detection
- MPAndroidChart: live magnetic field graph in SensorsFragment"
    success "Committed"
fi

# ── Step 5: Push ──────────────────────────────────────────────────────────────
echo ""
echo -e "${YELLOW}When prompted — Username: ${GITHUB_USER}  |  Password: GitHub PAT (not your password)${NC}"
echo -e "${YELLOW}Create PAT at: https://github.com/settings/tokens (scope: repo)${NC}"
echo ""
git push -u origin "$BRANCH"

echo ""
success "Done! Track build: https://github.com/${GITHUB_USER}/${REPO_NAME}/actions"
