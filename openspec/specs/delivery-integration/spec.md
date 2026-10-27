# delivery-integration Specification

## Purpose

Captures cross-feature guarantees that a delivery as a whole must satisfy: the reference execution flow, controlled handling of unprocessable input and the mandatory technical restrictions.

## Requirements

### Requirement: Delivery 1 reference flow
The application SHALL execute the Delivery 1 basic example — two USER CREATE, two USER UPDATE (PHONE, EMAIL), one USER DELETE, HELP and EXIT — producing `USER CREATED`, `USER CREATED`, `USER UPDATED`, `USER UPDATED`, `USER DELETED`, the help text, and then terminating, both interactively and from `input.txt`.

#### Scenario: Example executed from a file
- **WHEN** `java -jar target/nombre-aplicacion.jar input.txt` runs the example sequence from §16 on an empty system
- **THEN** the outputs appear in order and the stored users file contains only Ana with telephone `699999999` and email `ana.nueva@correo.com`

#### Scenario: Example executed interactively
- **WHEN** the same sequence is typed on standard input on an empty system
- **THEN** the output is identical to the file run

### Requirement: Unprocessable input handled
Malformed lines, unknown commands, invalid data and unreadable command files SHALL each produce a controlled error message without a stack trace or unexpected termination.

#### Scenario: Mixed invalid input
- **WHEN** a command file contains an unknown command, a USER CREATE with missing fields, a USER DELETE for an unknown user and a valid USER CREATE
- **THEN** three error messages are printed followed by `USER CREATED`, and the exit status is 0

### Requirement: Delivery 1 technical restrictions
The project SHALL be a standard Maven project for Java 21 built with `mvn package`, use only the Java standard library at runtime, use no regular expressions, databases or frameworks, and keep the user, main and persistence classes separate.

#### Scenario: Restrictions verified on the sources
- **WHEN** the production sources are inspected
- **THEN** they import only `java.*` and project packages, contain no regex API usage, and user, main and persistence logic live in different classes
