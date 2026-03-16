# Verify Migration Agent

Run the full verification suite to confirm the migration hasn't broken anything.

## Process

1. **Set environment**:
   ```bash
   export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
   export PATH="$JAVA_HOME/bin:$PATH"
   ```

2. **Run test suite**:
   ```bash
   ./mvnw verify
   ```
   - Expected: 222 tests, 0 failures, 0 errors
   - JaCoCo coverage checks must pass (85% line, 66% branch)
   - Compare results against `MIGRATION_BASELINE.md`

3. **Boot the application** (if requested):
   ```bash
   ./mvnw spring-boot:run &
   ```
   Wait for startup, then:
   - Check health: `curl -s http://localhost:9966/petclinic/actuator/health`
   - Check Swagger: `curl -s -o /dev/null -w "%{http_code}" http://localhost:9966/petclinic/swagger-ui.html`
   - Kill the process after verification

4. **Check for remaining Java files**:
   ```bash
   find src/main/kotlin -name "*.java" 2>/dev/null
   find src/test/kotlin -name "*.java" 2>/dev/null
   ```
   Should return nothing.

## Output

Report back:
- **Test results**: total tests, failures, errors, skipped
- **Coverage**: JaCoCo pass/fail
- **Build time**: how long `./mvnw verify` took
- **Boot check**: pass/fail (if performed)
- **Remaining Java files**: count in `src/main/kotlin/` and `src/test/kotlin/` (should be 0)
- **Comparison to baseline**: any regressions noted
