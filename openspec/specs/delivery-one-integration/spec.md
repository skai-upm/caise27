# delivery-one-integration Specification

## Purpose

Captures the complete state of the first delivery: all features working together under the delivery's technical restrictions, in both execution modes.

## Requirements

### Requirement: End-to-end delivery behaviour
All delivery-1 commands SHALL work together in interactive and file mode: accepted operations persist, rejected operations leave state uncorrupted, and no input causes uncontrolled termination.

#### Scenario: Source example sequence
- **WHEN** the source `input.txt` example (create Ana, create Pedro, update Ana's phone and email, delete Pedro, HELP, EXIT) runs from a file
- **THEN** each step reports success, only Ana remains with the updated phone and email, and the result persists

#### Scenario: Mixed valid and invalid input
- **WHEN** a sequence mixes valid commands with malformed, unknown and disallowed ones
- **THEN** valid commands take effect, invalid ones report errors, and the stored data stays consistent

### Requirement: Delivery-1 technical restrictions
The production code SHALL use Java 21 and the Java standard library, with no databases, frameworks, regular expressions or external libraries other than the BCrypt library that Delivery 3 explicitly requires for password protection, and SHALL build from scratch with `mvn package` into a JAR runnable outside an IDE.

#### Scenario: Fresh build
- **WHEN** `mvn package` runs on a fresh checkout
- **THEN** the executable JAR is produced and runs both interactively and with a command file

#### Scenario: Restriction inspection
- **WHEN** the production sources and `pom.xml` are inspected
- **THEN** the only runtime dependency is the BCrypt library and there is no use of regular expressions or other encryption
