# Proposal

## Why

The Amusement Park project (Delivery 1) starts from an empty repository. It needs a Maven/JDK 21 base generated from `maven-archetype-quickstart` that can be packaged as an executable JAR, followed by the first functional epic: terminal-driven user management (create, delete, update) with strict data validation, uniqueness rules and file persistence. The same delivery also requires running commands both interactively and from a command file, plus `HELP` and `EXIT` general commands.

## What Changes

- Generate the project skeleton with `org.apache.maven.archetypes:maven-archetype-quickstart`, compiling with JDK 21 (`maven.compiler.release=21`), keeping the archetype structure (`src/main/java`, `src/test/java`, `pom.xml`) and its test scaffolding.
- Configure the build so `mvn package` produces an executable JAR whose manifest points at the main class; `mvn test` must pass.
- Add a text command interface where every command is split with `String.split` (`;` separates the operation from its parameters):
  - `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`
  - `USER DELETE;Correo;Contraseña`
  - `USER UPDATE;Correo;Contraseña;Campo;NuevoValor` with editable fields `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD` (DNI never editable).
  - `HELP` (lists available commands and syntax) and `EXIT` (controlled termination).
- Validate user data without regular expressions or external libraries: Spanish DNI (8 digits + check letter computed with the official modulo-23 algorithm), email (`texto@texto.xxx`, 3-character extension), password (upper, lower, digit, symbol), card number (exactly 16 digits). Telephone is stored only.
- Enforce uniqueness of DNI and email.
- Persist users to a `;`-separated text file (one user per line) and reload them at startup, through a persistence class separate from the `User` class and the main class.
- Support two execution modes sharing one command-processing path: interactive (no arguments, reads standard input until `EXIT` or end of input) and file mode (first argument is a command file, processed line by line, blank lines ignored, stops at `EXIT`). Unreadable/nonexistent files are reported in a controlled manner.
- Invalid commands or missing parameters are reported without terminating the program.
- Out of scope: attractions, groups, tickets, fast passes, encryption, frameworks, databases, regular expressions, external libraries.

## Capabilities

### New Capabilities
- `project-build`: Maven/JDK 21 project structure, executable JAR packaging and test execution.
- `user-management`: User data model, validation rules, uniqueness, and the create/delete/update user operations.
- `user-persistence`: Storage of users in a semicolon-separated text file and their recovery when the application starts.
- `command-processing`: Text command interpretation, interactive and file execution modes, `HELP`, `EXIT`, and handling of invalid commands.

### Modified Capabilities
<!-- None: there are no existing specs in this project. -->

## Impact

- New `pom.xml` and `src/` tree at the repository root (greenfield; no existing code affected).
- New runtime data file (`data/users.txt`) created by the application; it is local state and should not be committed.
- No external dependencies beyond the archetype's JUnit test dependency.
