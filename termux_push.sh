#!/data/data/com.termux/files/usr/bin/bash
# ============================================================
#  SentinelOS — Termux Setup & GitHub Push Script
#  Run once from inside the SentinelOS project folder
#  Usage:  bash termux_push.sh
# ============================================================

set -e
RED='\033[0;31m'; GREEN='\033[0;32m'; CYAN='\033[0;36m'; NC='\033[0m'
info()    { echo -e "${CYAN}[INFO]${NC}  $1"; }
success() { echo -e "${GREEN}[OK]${NC}    $1"; }
fail()    { echo -e "${RED}[FAIL]${NC}  $1"; exit 1; }

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
GITHUB_USER="mukhteghwagae-gif"
REPO_NAME="SentinelOS"
BRANCH="main"

cd "$PROJECT_DIR"
info "Project root: $PROJECT_DIR"

# ── Step 1: Install required packages ────────────────────────────────────────
info "Checking Termux packages..."
pkg install -y git openjdk-17 gradle 2>/dev/null || true
success "Packages ready"

# ── Step 2: Generate gradle wrapper (needs gradle installed) ─────────────────
if [ ! -f "gradle/wrapper/gradle-wrapper.jar" ]; then
    info "Generating Gradle wrapper..."
    gradle wrapper --gradle-version 8.4 --distribution-type bin
    success "Gradle wrapper generated"
else
    success "Gradle wrapper already present"
fi

chmod +x gradlew

# ── Step 3: Git init / remote ─────────────────────────────────────────────────
if [ ! -d ".git" ]; then
    info "Initialising git repository..."
    git init
    git branch -M "$BRANCH"
    success "Git initialised"
fi

# Set remote (update URL if already set)
if git remote get-url origin &>/dev/null; then
    git remote set-url origin "https://github.com/${GITHUB_USER}/${REPO_NAME}.git"
    info "Remote origin updated"
else
    git remote add origin "https://github.com/${GITHUB_USER}/${REPO_NAME}.git"
    info "Remote origin added"
fi

# ── Step 4: Git identity (skip if already set) ────────────────────────────────
if [ -z "$(git config user.email)" ]; then
    git config user.email "sentinel@sentinelos.app"
    git config user.name  "SentinelOS Build"
fi

# ── Step 5: Stage and commit ──────────────────────────────────────────────────
info "Staging all files..."
git add -A

if git diff --cached --quiet; then
    info "Nothing new to commit — already up to date"
else
    COMMIT_MSG="fix: SentinelOS fully corrected build

- Added runtime permissions (BLE Android 12+, Camera, Location)
- Fixed crash: NotificationChannel created in Application.onCreate()
- Fixed crash: All Services declared in AndroidManifest with foregroundServiceType
- Fixed crash: BLE scanner null-checked + SecurityException guards
- Fixed crash: Sensor null-checks for missing hardware
- Fixed: All R.id mismatches between Kotlin and XML resolved (100%)
- Added: Working NightGuardService with real accelerometer motion detection
- Added: MagnetometerManager reading 6 sensors with anomaly detection
- Added: MPAndroidChart live magnetic field graph in SensorsFragment
- Added: BleScanner with RSSI colour coding and distance estimation
- Added: 5-tab navigation (Dashboard, Sensors, BLE, Night Guard, Log)
- Added: BootReceiver for auto-start on device boot
- Added: GitHub Actions CI/CD building debug + release APK"

    git commit -m "$COMMIT_MSG"
    success "Committed"
fi

# ── Step 6: Push ──────────────────────────────────────────────────────────────
info "Pushing to GitHub (${GITHUB_USER}/${REPO_NAME} — branch: ${BRANCH})..."
echo ""
echo -e "${CYAN}You will be prompted for your GitHub username and Personal Access Token.${NC}"
echo -e "${CYAN}Generate a token at: https://github.com/settings/tokens${NC}"
echo -e "${CYAN}Scopes needed: repo (full)${NC}"
echo ""

git push -u origin "$BRANCH"

echo ""
success "Push complete!"
echo ""
echo -e "${CYAN}GitHub Actions will now build your APK automatically.${NC}"
echo -e "${CYAN}Track progress at:${NC}"
echo -e "  https://github.com/${GITHUB_USER}/${REPO_NAME}/actions"
echo ""
echo -e "${CYAN}Download APK from:${NC}"
echo -e "  Actions → latest run → Artifacts → SentinelOS-debug-*"
