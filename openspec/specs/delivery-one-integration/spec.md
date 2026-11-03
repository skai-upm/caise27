# delivery-one-integration Specification

## Purpose

Captures the delivery-wide technical restrictions and end-to-end robustness that the complete first delivery must satisfy (source F1-C09, sections 13, 16 and 17).

## Requirements

### Requirement: Delivery 1 technical restrictions
Delivery 1 SHALL use Java 21, Maven with the standard structure, the Java standard library only at runtime, file-based persistence, and command processing with basic String operations and `split`; it SHALL NOT use databases, frameworks, external runtime libraries, regular expressions or encryption mechanisms. From Delivery 3 the BCrypt library added through Maven for password protection is the only permitted exception to the library and encryption restrictions.

#### Scenario: Runtime dependencies are standard library only
- **WHEN** the `pom.xml` and source code are inspected
- **THEN** apart from the Delivery 3 BCrypt dependency there are no non-test dependencies, and no regular expression APIs are used in the application code

### Requirement: End-to-end controlled behavior
All Delivery 1 features SHALL work together from a fresh build: accepted operations SHALL be persisted, and rejected or malformed input SHALL be reported without terminating the application and without corrupting in-memory or stored state.

#### Scenario: Example session from the assignment
- **WHEN** the example file creating Ana and Pedro, updating Ana's PHONE and EMAIL, deleting Pedro, then `HELP` and `EXIT` is run with `java -jar` on a fresh build
- **THEN** each operation reports success, and the stored data contains only Ana with telephone `699999999` and email `ana.nueva@correo.com`

#### Scenario: Mixed valid and invalid commands
- **WHEN** a file mixes valid commands with invalid ones (bad DNI, duplicate email, wrong password, unknown command, wrong number of parameters)
- **THEN** every invalid command reports an error, the valid ones take effect, and the application processes the whole file without crashing
