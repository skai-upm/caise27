# Proposal

## Why

The repository is empty: there is no application yet. Delivery 1 of the Amusement Park assignment (source: `.botl/proposals/opus_4_description_nl/0001-1.D1.-description.md/1.D1.-description.md`) asks for the foundation of a Java application to manage an amusement park. It covers only user management and the infrastructure to run the application: a Maven build that produces an executable JAR, a text-command interface that works interactively or from a file, and file-based persistence. Park-specific features will come in later deliveries, so this foundation must keep responsibilities cleanly separated.

## What Changes

- Create a Maven project (standard layout, quickstart-archetype structure) targeting Java 21 that builds with `mvn package` into an executable JAR runnable with `java -jar target/<name>.jar [input.txt]`.
- Introduce a user domain object with encapsulated attributes (first name, surnames, DNI, email, password, telephone, card number) and an immutable DNI.
- Add user data validation without regular expressions or external libraries: Spanish DNI check letter, basic `text@text.xxx` email format, password composition (upper, lower, digit, symbol), and 16-digit card number.
- Add the `USER CREATE`, `USER DELETE`, and `USER UPDATE` commands, enforcing DNI and email uniqueness, password-checked deletion and modification, and an enum of modifiable fields (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`).
- Persist users in a `;`-separated text file (one user per line), loaded on startup and rewritten after every change, managed by a class separate from both the user model and the main class.
- Add a `;`-separated command interpreter built on basic `String` operations and `split`, with `HELP` and `EXIT` commands. It is shared by interactive mode (standard input) and file mode (first program argument). Invalid input never terminates the application unexpectedly.

## Capabilities

### New Capabilities
- `application-build`: How the application is built and launched: Java 21 Maven project, `mvn package` producing an executable JAR, and selecting interactive or file mode from program arguments.
- `command-interface`: Text-command language and execution loop: `;`-separated parsing, the shared processing of commands from standard input or a command file, `HELP`, `EXIT`, and handling of malformed or unknown commands.
- `user-management`: User accounts: attributes and invariants, validation rules for DNI/email/password/card, uniqueness, and the create/delete/update operations exposed as `USER` commands.
- `user-persistence`: Durable storage of users between runs in a `;`-separated text file, loaded at startup and kept up to date after each change.

### Modified Capabilities
<!-- None: the project has no existing specs. -->

## Impact

- New code: `pom.xml`, `src/main/java/...` (application entry point, user model, field enum, validator, user service, persistence, command processing), `src/test/java/...` (JUnit tests).
- New runtime artifact: `target/amusement-park.jar` (executable) and a `users.txt` data file created in the working directory at runtime.
- Dependencies: Java standard library only at runtime. JUnit 5 is used with `test` scope for automated tests and is not packaged into the JAR.
- Out of scope: the assignment's mandatory Git milestone commit history (`[F0-C01]` … `[F1-C09]`) is not reproduced. Commits in this repository are made by the BOTL batch workflow, one per proposal (see design.md).
