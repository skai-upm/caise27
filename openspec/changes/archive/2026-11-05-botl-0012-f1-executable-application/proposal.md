# Proposal

## Why

Milestone F0-C02 requires the Maven project to become a standalone application: `mvn package` must produce a JAR that starts with `java -jar`, with a correctly declared entry point, waiting in interactive mode on standard input when started without arguments, and processing a text file given as an argument. Because of batch ordering, this behavior was already delivered by an earlier change (`application-runtime`, Delivery 1 integration). What's missing is the milestone's explicit contract (a declared entry point and interactive waiting) and an automated guard for it.

## What Changes

- Tighten the existing `application-runtime` requirement "Executable JAR produced by Maven" (**MODIFIED**): the manifest declares the entry point class, the application waits on standard input without arguments, and it processes a text file argument.
- Add automated tests checking that `pom.xml` declares the manifest main class, that the class exists with a `public static void main(String[])`, and that interactive processing keeps reading until `EXIT`.
- No production code changes: the existing JAR configuration already satisfies the milestone.

### Out of scope
- Command-file semantics beyond starting (F1-C07) and `--fly` (already specified).

## Capabilities

### New Capabilities
<!-- None: the suggested `executable-application` capability would duplicate `application-runtime`. -->

### Modified Capabilities
- `application-runtime`: the executable-JAR requirement gains the explicit entry-point and interactive-mode clauses.

## Impact

- Tests only (`ExecutableJarTest`) plus spec text.
- Verified manually with `mvn package` and `java -jar`.
