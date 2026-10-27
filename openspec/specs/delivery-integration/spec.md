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

### Requirement: Delivery 2 reference flow
Phase 1 and Phase 2 commands SHALL coexist: a command file mixing user, system, group, ticket, group ticket and attraction commands SHALL run from `java -jar target/nombre-aplicacion.jar input_fase2.txt` and from `java -jar target/nombre-aplicacion.jar --fly input_fase2.txt` with identical output, the latter leaving no data behind.

#### Scenario: Mixed command file in both modes
- **WHEN** the same Phase 2 command file runs once normally on an empty data directory and once with `--fly`
- **THEN** both outputs are identical and only the normal run leaves data files

### Requirement: Delivery 2 technical restrictions
The application SHALL keep the Delivery 1 restrictions and SHALL represent relationships with object references in the specified directions: users hold no references to tickets, groups or attractions; groups hold no references to tickets; the central park controller has a single instance.

#### Scenario: Relationship directions verified
- **WHEN** the user and group classes are inspected
- **THEN** they declare no fields of ticket, group or attraction types (besides a group's members)

### Requirement: Delivery 3 command coverage in help
HELP SHALL list the syntax of every Delivery 1, 2 and 3 command, including `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`, `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, the SPEEDPASS, SPECIALPASS and INVOICE commands, `HELP`, `HELP EXTENDED` and `EXIT`, and HELP EXTENDED SHALL give a description for each.

#### Scenario: Required syntax present
- **WHEN** `HELP` and `HELP EXTENDED` are processed
- **THEN** every required syntax line appears in both, each followed by a description in the extended output

### Requirement: Delivery 3 reference flow
A command file mixing Delivery 1, 2 and 3 commands SHALL produce identical output when run with `java -jar target/nombre-aplicacion.jar input_fase3.txt` and with `--fly input_fase3.txt`, the latter leaving no data behind.

#### Scenario: Mixed Phase 3 file in both modes
- **WHEN** the same Phase 3 command file runs once normally on an empty data directory and once with `--fly`
- **THEN** both outputs are identical, include pass consumption and invoices, and only the normal run leaves data files (with consumed passes still stored)

### Requirement: Delivery 3 technical restrictions
The application SHALL keep the earlier restrictions with BCrypt as the only non-JDK runtime library, and consumed passes SHALL never be deleted as a consequence of their use.

#### Scenario: Restrictions verified
- **WHEN** the production sources and the stored data after consuming passes are inspected
- **THEN** only `java.*`, project and `org.mindrot.jbcrypt` imports are used, and consumed passes are still stored
