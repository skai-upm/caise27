# delivery-one-integration Specification

## Purpose

Captures the Delivery 1 technical restrictions and the integrated, end-to-end behavior of all first-delivery features working together.

## Requirements

### Requirement: Delivery 1 technical restrictions
Delivery 1 SHALL use Java 21, a standard-structure Maven project built with `mvn package`, an executable JAR, file-based persistence, command processing with basic `String` operations and `split`, separated user, main and persistence classes, and encapsulated attributes. It SHALL NOT use databases, frameworks, regular expressions, or external runtime libraries and encryption other than those a later delivery explicitly authorizes (the Delivery 3 BCrypt dependency for password protection).

#### Scenario: Restriction inspection
- **WHEN** `pom.xml` and the main sources are inspected
- **THEN** there are no runtime dependencies beyond the JDK and the authorized BCrypt library, and no regular expression APIs are used

### Requirement: Integrated command flow
All Delivery 1 commands SHALL work together in a single run and across runs: accepted operations persist, and rejected or unprocessable input is reported without corrupting state or terminating the application.

#### Scenario: Basic execution example
- **WHEN** the file `USER CREATE` Ana, `USER CREATE` Pedro, `USER UPDATE` Ana PHONE `699999999`, `USER UPDATE` Ana EMAIL `ana.nueva@correo.com`, `USER DELETE;pedro@correo.com;Otra2#`, `HELP`, `EXIT` is processed by the packaged JAR
- **THEN** the output reports `USER CREATED`, `USER CREATED`, `USER UPDATED`, `USER UPDATED`, `USER DELETED`, the help text, and storage contains only Ana with telephone `699999999` and email `ana.nueva@correo.com`

#### Scenario: Mixed valid and invalid sequence
- **WHEN** a sequence mixing valid commands with invalid data, duplicates, wrong credentials and unknown commands is executed interactively or from a file
- **THEN** only the valid operations change stored users and processing continues after each rejection
