# Design

## Context

The repository is empty apart from OpenSpec scaffolding, a README, a LICENSE and a Maven-oriented `.gitignore` (which already ignores `target/`). The local toolchain is Maven 3.9 with a JDK newer than 21, so the build must pin the bytecode level with `maven.compiler.release=21` instead of relying on the installed JDK. Motivation and scope: see proposal.md. Behaviour: see the five spec deltas.

## Goals / Non-Goals

**Goals:**
- A small, flat class structure that satisfies the source's separation rule (user class ≠ main class ≠ persistence class) and lets later deliveries add domain concepts.
- One command-processing path shared by interactive and file mode.
- Validation that is unit-testable in isolation and reusable by create and update.

**Non-Goals:**
- Layered package reorganisation (that is a later delivery's milestone, F3-C01).
- Any Delivery 2 concept (system date, price, groups, tickets, attractions).
- Producing the per-milestone Git commits (see proposal, flagged decision 1).

## Decisions

1. **Coordinates and layout.** `groupId=es.upm.amusementpark`, `artifactId=amusement-park`, single package `es.upm.amusementpark`. The archetype layout is reproduced by hand (no network-dependent `archetype:generate` run), yielding the same structure. *Alternative:* running the archetype — rejected because it is equivalent and adds a network dependency to the build step.

2. **Executable JAR.** `maven-jar-plugin` with `<archive><manifest><mainClass>es.upm.amusementpark.App</mainClass>` and `<finalName>amusement-park</finalName>`. No shading is needed because there are no runtime dependencies. *Alternative:* `maven-shade-plugin` — unnecessary.

3. **Classes.**
   - `App` — entry point only: chooses interactive vs file mode, wires collaborators, contains no file I/O logic.
   - `User` — encapsulated attributes; `dni` is `final` with no setter.
   - `UserField` — enum `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`.
   - `UserValidator` — static checks `isValidDni/Email/Password/Card`, using `charAt`, `Character.isDigit/isLetter/isUpperCase/isLowerCase/isWhitespace`, `indexOf`, `split`. No `java.util.regex`, no `matches`.
   - `UserRepository` — owns the in-memory list and the text file; `load()`, `save()`, lookup by email/DNI. The only class that reads or writes the users file.
   - `UserService` — create/delete/update business rules (validation, uniqueness, credential checks); throws `IllegalArgumentException` with a message on rejection, then persists through the repository.
   - `CommandProcessor` — parses one line (`split(";", -1)`), dispatches, returns the output text and whether EXIT was requested. Never throws for bad input.
   - `CommandRunner` — loops over a `BufferedReader` (stdin or file), skips blank lines, prints results, stops on EXIT. Used by both modes so processing is identical.

4. **Parsing.** The first element is the command (e.g. `USER CREATE`); it is trimmed and matched exactly (upper case). Values are kept as given except surrounding whitespace trimmed. Exact argument counts are enforced; blank values are rejected (proposal decision 8).

5. **Email split note.** `String.split` takes a regex pattern argument, but the source explicitly allows split; only literal separators (`"@"`, `";"`) are used, and the extension is found with `lastIndexOf('.')` so no regex metacharacter is ever relied upon.

6. **Persistence timing.** Save the whole file after every successful mutation (write-through). The data set is tiny, so simplicity wins over incremental writes. Load once at start-up; a missing file means no users; a malformed line (wrong field count) is skipped with a warning on stderr rather than aborting start-up.

7. **Users file path.** `users.txt` in the working directory, injectable through the `UserRepository` constructor so tests use a temp directory.

8. **Tests.** JUnit 5 (test scope only) covering validators, service operations, persistence round-trip, command parsing, file mode, HELP/EXIT. Plus an end-to-end check running the packaged JAR.

## Risks / Trade-offs

- [Passwords stored in plain text] → Required by the source for Delivery 1 ("encryption mechanisms" are forbidden); revisited by a later milestone.
- [A value containing `;` cannot be represented] → Inherent in the `;`-separated command and file formats; such input is split and therefore rejected by argument-count checks.
- [Write-through rewrites the whole file each time] → Acceptable at this data size.
- [`users.txt` created in the repo root during manual runs] → Add `users.txt` to `.gitignore`.
