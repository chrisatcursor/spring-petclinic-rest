# Security Scan Agent

Audit the security configuration after migration to verify no auth behavior has changed.

## Scope

Focus on these files and their Kotlin equivalents:
- `security/BasicAuthenticationConfig` -- Spring Security filter chain, JDBC auth
- `security/DisableSecurityConfig` -- conditional security bypass
- `security/Roles` -- role constants used in `@PreAuthorize`
- All `@PreAuthorize` annotations on controller methods
- CORS configuration (`@CrossOrigin` on controllers)

## Checks

### 1. Filter Chain Integrity
- Verify the `SecurityFilterChain` bean configuration is functionally identical
- CSRF disabled, all requests require authentication, HTTP Basic enabled
- JDBC authentication query strings unchanged

### 2. Role-Based Access
- Compare all `@PreAuthorize("hasRole(@roles.XXX)")` annotations before and after
- Verify the `Roles` component exposes the same constants
- Check no `@PreAuthorize` annotations were accidentally removed

### 3. Conditional Configuration
- `@ConditionalOnProperty(name = "petclinic.security.enable", havingValue = "true")` must be preserved
- `DisableSecurityConfig` must still activate when security is disabled

### 4. CORS Configuration
- `@CrossOrigin(exposedHeaders = "errors, content-type")` on all controllers
- Verify no changes to exposed headers or allowed origins

### 5. Password Encoding
- `BCryptPasswordEncoder` bean must still be configured

## Process

1. Read the original Java security files (from git history if already converted)
2. Read the Kotlin equivalents
3. Perform a semantic diff -- are they functionally identical?
4. Scan all controller files for `@PreAuthorize` annotations
5. Verify CORS annotations on all `@RestController` classes

## Output

Report:
- **Filter chain**: PASS/FAIL with details
- **Role annotations**: PASS/FAIL with list of all `@PreAuthorize` found
- **Conditional config**: PASS/FAIL
- **CORS**: PASS/FAIL
- **Password encoding**: PASS/FAIL
- **Overall**: PASS/FAIL with any concerns flagged
