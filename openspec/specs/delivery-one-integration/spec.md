# delivery-one-integration Specification

## Purpose

Captures the Delivery 1 technical restrictions and the end-to-end behavior expected when all first-delivery features work together.

## Requirements

### Requirement: Delivery 1 technical restrictions
Delivery 1 SHALL use Java 21, the standard Maven structure, build with `mvn package` into an executable JAR, run interactively and with a `.txt` command file, use only the Java standard library at runtime, persist to files, interpret commands with basic String operations and `split`, keep the user, main and persistence classes separate, and encapsulate attributes. It SHALL NOT use databases, frameworks, external libraries, regular expressions or encryption, except that from Delivery 3 the BCrypt library is used to protect passwords. (F1-C09, F3-C05)

#### Scenario: Fresh build
- **WHEN** `mvn package` runs on a fresh checkout
- **THEN** `target/nombre-aplicacion.jar` is produced and runs independently of an IDE

#### Scenario: Dependency inspection
- **WHEN** the runtime dependencies of the build are inspected
- **THEN** none are declared beyond the Java standard library, except the BCrypt dependency added in Delivery 3

### Requirement: Controlled end-to-end processing
When valid and invalid commands are mixed, in either input mode, accepted operations SHALL take effect and persist, and rejected operations SHALL leave user state and the users file uncorrupted. (F1-C09)

#### Scenario: Reference execution
- **WHEN** the file creates Ana and Pedro, updates Ana's phone and email, deletes Pedro, then runs `HELP` and `EXIT`
- **THEN** the output reports `USER CREATED`, `USER CREATED`, `USER UPDATED`, `USER UPDATED`, `USER DELETED`, the help text, and only Ana (with phone `699999999` and email `ana.nueva@correo.com`) remains stored

#### Scenario: Mixed valid and invalid commands
- **WHEN** a sequence interleaves valid commands with invalid DNIs, duplicate emails, wrong passwords and unknown commands
- **THEN** only the valid commands change state and the application never terminates unexpectedly
