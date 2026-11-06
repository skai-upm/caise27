# Spec Delta

## Purpose

Captures the complete Delivery 1 behavior of the Amusement Park application as a whole: user management end to end in interactive and file mode, built from scratch with Maven under the delivery's technical restrictions.

## ADDED Requirements

### Requirement: Delivery 1 end-to-end flow
Built from a clean checkout with `mvn package`, the JAR SHALL execute the source section 16 sequence (create Ana, create Pedro, update Ana's phone, update Ana's email, delete Pedro, HELP, EXIT) with equivalent results whether the commands come from standard input or from a command file given as first argument.

#### Scenario: Example sequence in file mode
- **GIVEN** a clean build and an `input.txt` with the section 16 commands
- **WHEN** `java -jar target/amusement-park.jar input.txt` runs in an empty directory
- **THEN** two creations, two updates and one deletion are confirmed, the help text and closing message are printed, and the user file holds only Ana with phone `699999999` and email `ana.nueva@correo.com`

#### Scenario: Example sequence in interactive mode
- **GIVEN** the same commands typed on standard input
- **WHEN** `java -jar target/amusement-park.jar` runs in an empty directory
- **THEN** the printed results and the final user file are identical to file mode

#### Scenario: Incorrect situations are handled
- **GIVEN** a command file mixing invalid data, duplicates, wrong passwords, unknown commands and malformed lines with valid commands
- **WHEN** it is processed
- **THEN** each incorrect line yields an error message, valid lines take effect, and the application ends normally

### Requirement: Delivery 1 technical restrictions
The production code SHALL use only the Java standard library, with no databases, frameworks, external libraries, regular expressions or encryption mechanisms, and commands MUST be interpreted with basic String operations and `split`.

#### Scenario: No external runtime dependencies
- **WHEN** the Maven project's dependencies are inspected
- **THEN** only test-scoped dependencies are declared

#### Scenario: No regular expressions
- **WHEN** the production sources are inspected
- **THEN** no regular-expression API (`Pattern`, `matches`, `replaceAll`, regex `split` patterns) is used
