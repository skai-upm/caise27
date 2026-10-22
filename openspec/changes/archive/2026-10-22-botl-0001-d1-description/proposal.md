# Proposal

## Why

Delivery 1 of the Amusement Park assignment requires the foundation of a Java 21 / Maven application: an executable JAR that manages park users through text commands, validates their data, and keeps them between runs. The repository currently contains no code, so this change establishes the project, the user domain, and the command infrastructure that later deliveries (attractions, tickets, groups) will build on.

## What Changes

- Create a standard Maven project (quickstart layout, JDK 21) that builds with `mvn package` into an executable JAR runnable with `java -jar target/<app>.jar [input.txt]`.
- Add a user domain with encapsulated attributes (first name, surnames, DNI, email, password, phone, card); DNI is immutable after creation.
- Add user data validation without regular expressions or external libraries: Spanish DNI check letter, basic `text@text.xxx` email format, password character classes, 16-digit card number.
- Add `USER CREATE`, `USER DELETE`, `USER UPDATE` commands with DNI/email uniqueness and email+password authentication for delete/update; updatable fields are represented by an enum (`NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD`).
- Add plain-text file persistence (one user per line, `;`-separated) handled by a dedicated class; users are loaded at startup and saved after each change.
- Add a `;`-separated command interpreter shared by interactive (stdin) and file mode, plus `HELP` and `EXIT`; invalid commands never crash the application, and `EXIT` stops processing of any later line.

## Capabilities

### New Capabilities
- `application-runtime`: Maven build, executable JAR, and the interactive / command-file execution modes.
- `command-interface`: Command syntax, dispatching, error handling for unknown or malformed commands, `HELP` and `EXIT`.
- `user-validation`: Validation rules for DNI, email address, password and card number.
- `user-management`: Creating, deleting and updating users, including uniqueness and credential checks and the immutability of the DNI.
- `user-persistence`: Storing users in a text file between runs and loading them at startup.

### Modified Capabilities

(none — no existing specs)

## Impact

- New code: `pom.xml`, `src/main/java/**`, `src/test/java/**` (greenfield).
- Runtime: the application creates/updates a `users.txt` data file in the working directory.
- Dependencies: Java standard library only at runtime; JUnit 5 is used with `test` scope only, so the delivered JAR has no external libraries.
- Out of scope: the assignment's mandatory student Git milestone commits (`[F0-C01]`…`[F1-C09]`) — commits in this repository are governed by the batch workflow, not by the course roadmap.
