# One-time setup before first push

The `gradle-wrapper.jar` cannot be bundled here (binary).
Run this once inside the project folder on your machine or in Termux:

```bash
# Option A — generate via gradle itself (if gradle is installed globally)
gradle wrapper --gradle-version 8.4

# Option B — copy from any existing Android project
cp ~/path/to/any-android-project/gradle/wrapper/gradle-wrapper.jar \
   gradle/wrapper/gradle-wrapper.jar

# Option C — Termux (recommended for you)
pkg install gradle
cd /path/to/SentinelOS
gradle wrapper --gradle-version 8.4
```

After that, push normally:
```bash
git add .
git commit -m "Initial SentinelOS fixed build"
git push origin main
```
