# Modernize Kotlin Agent

Review migrated Kotlin code for Java-isms and improve idiomaticness.

## What to Look For

Scan the Kotlin files in `src/main/kotlin/` and `src/test/kotlin/` for:

1. **Unnecessary `Unit` return types**: Functions that explicitly declare `: Unit` -- remove the return type
2. **Verbose null checks**: `if (x != null) { x.doSomething() }` -> `x?.doSomething()`
3. **`let` overuse**: `x?.let { it.doSomething() }` when `x?.doSomething()` suffices
4. **Mutable collections where immutable work**: `MutableList` used but never mutated after creation -> use `List`
5. **Missing `when` expressions**: Chain of `if/else if` on the same variable -> `when`
6. **Explicit `public` modifiers**: `public fun` or `public class` -> remove `public` (it's the default)
7. **Java-style string concatenation**: `"foo" + bar + "baz"` -> `"foo${bar}baz"`
8. **Unnecessary `.toString()` calls**: In string templates, `"${obj.toString()}"` -> `"$obj"`
9. **`it` parameter in single-parameter lambdas**: Named when it should be `it`, or `it` when a name would be clearer
10. **`emptyList()` / `emptySet()` / `emptyMap()`**: Using `listOf()` when `emptyList()` is more explicit
11. **`lateinit` where lazy delegation works**: `lateinit var` for computed properties -> `by lazy { }`
12. **Missing sealed classes/interfaces**: Where applicable for type hierarchies
13. **`companion object` for utility functions**: That could be top-level functions instead

## What NOT to Change

- Do NOT change any API behavior or signatures
- Do NOT restructure packages
- Do NOT modify generated code
- Do NOT change test assertions or expected values
- Do NOT introduce new dependencies

## Process

1. Scan all `.kt` files in `src/main/kotlin/` and `src/test/kotlin/`
2. For each file, note any Java-isms found
3. Apply fixes
4. Run `./mvnw compile` after changes to verify compilation
5. Run `./mvnw verify` to ensure tests still pass

## Output

Produce a changelog listing:
- File path
- What was changed
- Why (which Java-ism was addressed)

Format as a markdown list grouped by file.
