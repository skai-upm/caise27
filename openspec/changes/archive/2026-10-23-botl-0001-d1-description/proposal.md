# Proposal

## Why

The repository is empty: Delivery 1 of the Amusement Park assignment needs the foundation of a Java 21 / Maven application that can be built into an executable JAR and that manages park users through text commands, with validation and file-based persistence. Later deliveries (park-specific features) build on this foundation, so it must be in place first.

## What Changes

- Create a standard Maven project (quickstart layout) targeting Java 21 that builds with `mvn package` into an executable JAR (`target/amusement-park.jar`).
- Add a command-based text interface (`;`-separated, parsed with basic `String` operations and `split`, no regular expressions or external libraries) supporting `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP` and `EXIT`.
- Support two execution modes with identical command processing: interactive (standard input, no arguments) and file mode (path to a command file as the first argument).
- Introduce an encapsulated user model (first name, surnames, DNI, email, password, phone, card) whose DNI is immutable after creation.
- Validate DNI (8 digits + correct control letter), email (`text@text.xxx`), password (upper, lower, digit, symbol) and card number (exactly 16 digits) without regular expressions.
- Enforce uniqueness of DNI and email.
- Allow deletion (email + password) and modification of the fields `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD` (represented by an enum), re-applying creation rules.
- Persist users in a `;`-separated text file, one user per line, loaded at startup and rewritten after every change, implemented in a class separate from the user and main classes.
- Handle malformed or unknown commands and unreadable input files without terminating unexpectedly.

## Capabilities

### New Capabilities
- `command-interface`: Executable application entry, interactive and file execution modes, command parsing, `HELP`, `EXIT` and controlled handling of invalid input.
- `user-validation`: Rules that decide whether DNI, email, password and card number values are acceptable.
- `user-management`: Creating, deleting and updating users, including authentication by email/password, DNI immutability and DNI/email uniqueness.
- `user-persistence`: Storing users in a text file between runs and loading them at startup.

### Modified Capabilities
<!-- None: no existing specs in this project. -->

## Impact

- New files: `pom.xml`, `src/main/java/**`, `src/test/java/**`.
- Build/runtime: Java 21, Maven; no runtime dependencies beyond the Java standard library (JUnit is used only at test scope).
- Runtime data file: `users.txt` in the working directory (created on first write).
