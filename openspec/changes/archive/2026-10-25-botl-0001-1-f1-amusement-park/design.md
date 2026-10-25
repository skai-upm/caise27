# Design

## Context

Greenfield repository: only OpenSpec scaffolding, README and LICENSE exist. The local Maven repository already contains `maven-archetype-quickstart` 1.5 (JUnit 5 based). The JDK installed is newer than 21, so the build pins `maven.compiler.release=21`. See proposal.md for motivation; behavior is defined in the four delta specs.

## Goals / Non-Goals

**Goals:**
- Archetype-generated layout at the repository root, packaged as `target/parque.jar`.
- Small, explicit class responsibilities satisfying the mandated separation (User / main class / persistence class).
- One command-processing path shared by interactive and file modes.
- Unit tests for validation, user operations, persistence round trip and command processing.

**Non-Goals:**
- Layered packages, interfaces or design patterns (later deliveries).
- Hashing or encrypting passwords/telephones (later delivery).
- Concurrency or multi-process access to the data file.

## Decisions

1. **Project generation.** Run `mvn archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.5 -DgroupId=es.upm.parque -DartifactId=parque -Dpackage=es.upm.parque` in a scratch directory and move `pom.xml` and `src/` to the repository root (the repo already holds README/LICENSE/openspec, so generating in place would nest the project). Then set `maven.compiler.release` to 21 and configure `maven-jar-plugin` `archive.manifest.mainClass=es.upm.parque.App` and `<finalName>parque</finalName>`.
   *Alternative:* hand-writing a pom — rejected, the requirement explicitly names the archetype.

2. **Classes (package `es.upm.parque`).**
   - `App` — main class: chooses mode from `args`, opens standard input or the file, feeds lines to `CommandProcessor`, prints responses, reports file errors. Contains no persistence logic.
   - `CommandProcessor` — parses a line with `line.split(";", -1)`, dispatches on the first component (`USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP`, `EXIT`), checks parameter counts, returns a response string, exposes `isExitRequested()`. Catches unexpected runtime exceptions per command so nothing escapes.
   - `UserManager` — business operations (create/delete/update), uniqueness checks, credential checks; holds the in-memory user list and calls `UserRepository.save` after each successful change.
   - `UserValidator` — static validation helpers (DNI, email, password, card) using `charAt`, `Character.isDigit/isUpperCase/...`, `indexOf`, `split`. No regex.
   - `User` — private fields, getters, setters for editable fields only; DNI is `final`.
   - `UserField` — enum `NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD`, parsed from the command's `Campo` value.
   - `UserRepository` — constructed with a `Path`; `load()` returns `List<User>`, `save(List<User>)` rewrites the file. Only this class touches the file.
   *Alternative:* putting command handling in `App` — rejected because file and interactive modes must share processing and `App` should only manage terminal interaction.

3. **Parsing with limit `-1`.** `split(";", -1)` keeps trailing empty components so that `USER CREATE;...;` with an empty card is detected as an empty parameter instead of a missing one silently. Exact parameter counts are required; extra parameters are an error.

4. **Response format.** Successful operations print `OK <description>` (e.g. `OK USER CREATED`), failures print `ERROR <reason>`. This keeps output testable and is extended by later deliveries (`OK TICKET CREATED -> <Precio>`).

5. **Data file location.** `data/users.txt` relative to the working directory; parent directory created on save. Path is passed to the repository constructor so later deliveries (e.g. temporary execution modes) can reconfigure it. `data/` is added to `.gitignore`.

6. **Loading robustness.** Malformed lines (wrong number of fields) are skipped with a warning on stderr rather than aborting startup.

7. **Email comparison.** Emails are compared case-insensitively for lookup and uniqueness; DNI letters are normalised to upper case before validation and storage.

## Risks / Trade-offs

- [Field values containing `;` would corrupt the file] → impossible to enter through commands because `;` is the command separator.
- [Plaintext passwords in `data/users.txt`] → accepted for Delivery 1 (encryption explicitly out of scope); file is gitignored.
- [Whole-file rewrite on each change] → acceptable for the expected data size; simplest correct approach.
