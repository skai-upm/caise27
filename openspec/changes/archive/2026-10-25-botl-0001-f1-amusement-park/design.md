# Design

## Context

The repository contains only OpenSpec scaffolding; there is no Java code yet (see proposal.md - Why). The toolchain available locally is Maven 3.9 with JDK 24/25; no JDK 21 is installed, so Java 21 compatibility is enforced through `maven.compiler.release=21`, which makes `javac` compile against the Java 21 API and language level. Requirements are in `specs/`.

## Goals / Non-Goals

**Goals:**
- A quickstart-archetype project that builds, tests and packages as an executable JAR.
- A small set of classes with the three mandated responsibility boundaries: user state (`User`), application start-up and terminal/file interaction (`App`), and file storage (`UserRepository`).
- One command-processing path shared by interactive and file modes, testable without a real console.

**Non-Goals:**
- Interfaces, frameworks, dependency injection, or patterns aimed at later deliveries.
- Encryption, password hashing, or telephone validation.

## Decisions

### Project generation
Generate with `mvn archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.5 -DgroupId=com.amusementpark -DartifactId=amusement-park -DinteractiveMode=false` in a scratch directory and move the generated `pom.xml` and `src/` to the repository root, so the archetype's layout and plugin configuration are kept unchanged. The only POM edits are setting `maven.compiler.release` to `21` and adding a `maven-jar-plugin` manifest `mainClass` of `com.amusementpark.App`. Archetype 1.5 ships JUnit 5 as a test-scoped dependency.
*Alternative:* hand-writing the POM — rejected because the requirement is to retain the archetype output.

### Classes (package `com.amusementpark`)
- `App` — `main(String[] args)`: chooses the input source (stdin when `args.length == 0`, else `args[0]` opened with `Files.newBufferedReader`), creates a `UserRepository` for `users.txt` and a `CommandProcessor`, and feeds lines to it until it returns "stop" or input ends. Catches `IOException`/`InvalidPathException` when opening or reading the file and prints a controlled error. Contains no file-storage logic for users.
- `CommandProcessor` — `boolean process(String line)`: trims, ignores blank lines, splits with `line.split(";", -1)`, dispatches on the first component (`USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP`, `EXIT`), checks parameter counts, and writes messages to an injected `PrintStream`. Returns `false` only for `EXIT`. Both modes call this same method, so there is no duplicated command logic.
- `UserManager` — holds the in-memory `List<User>`, enforces uniqueness and credential checks, applies create/delete/update, and calls `UserRepository.save(users)` after each successful change. Returns a result message (success or reason for failure). Keeps business rules out of the command parser.
- `User` — private fields, getters, setters for all fields except DNI (`final`). No I/O.
- `UserField` — enum `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`; parsed with a loop over `values()` (not `valueOf`, to avoid exceptions for unknown fields).
- `Validator` — static `isValidDni`, `isValidEmail`, `isValidPassword`, `isValidCard` using `charAt`, `Character.isDigit/isUpperCase/isLowerCase/isLetterOrDigit/isWhitespace`, `indexOf`, `lastIndexOf` and `split`. No regex.
- `UserRepository` — `List<User> load()` and `void save(List<User>)`; serializes each user as one `;`-joined line, rewrites the whole file on save, skips lines that do not have exactly 7 fields on load, returns an empty list when the file is missing.

*Alternative:* putting create/update/delete logic directly in `CommandProcessor` — workable, but separating it keeps parsing (format/arity) from business rules and makes both easier to unit-test without adding speculative abstraction.

### Email check detail
Split on `@` with limit `-1`; require exactly 2 parts, both non-empty. On the domain part, use `lastIndexOf('.')`; require the domain name before it to be non-empty and the extension after it to have exactly 3 characters. Field values must not contain `;` (they would break the file format); this is naturally guaranteed because commands are split on `;`.

### Output convention
Success messages start with `OK:` and failures with `ERROR:` so tests and users can distinguish them. Exact wording is not part of the spec.

### Users file location
`users.txt` in the current working directory. Tests construct `UserRepository` with a temporary path.

## Risks / Trade-offs

- [No JDK 21 installed locally] → `release=21` guarantees the Java 21 API surface and bytecode level; running on a JDK ≥ 21 is required.
- [Passwords stored in plain text] → Explicitly mandated for this epic (no encryption yet); revisit when encryption is introduced.
- [Whole-file rewrite on every change] → Acceptable for the small data set of this delivery; keeps persistence simple.
- [Archetype download requires network access to Maven Central] → If unavailable, the build cannot be produced; treated as a blocker rather than hand-writing an equivalent.
