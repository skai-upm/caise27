# Spec Delta

## Purpose

Defines what the complete Delivery 3 application guarantees as a whole: an executable JAR, the Delivery 2 execution modes, every E1, E2 and E3 command working together, complete help, persistence in normal and temporary modes, and a class model matching the code.

## ADDED Requirements

### Requirement: Executable delivery
`mvn package` SHALL generate `target/parque.jar`, executable with `java -jar` in interactive mode, with a command file argument, and with `--fly`.

#### Scenario: Packaged JAR runs a command file
- **WHEN** `java -jar target/parque.jar examples/input_fase3.txt` is executed after `mvn package`
- **THEN** every command of the file is processed and the application ends at `EXIT`

### Requirement: All commands work together
The Delivery 1, 2 and 3 commands SHALL remain available and work together through the same command system, controllers and repositories, so that a single session can register users, form groups, sell individual and group tickets, buy passes, create attractions of every type, enter them with and without passes, generate invoices and query status.

#### Scenario: Delivery 3 script
- **WHEN** the Delivery 3 example script is executed
- **THEN** each command expected to succeed prints an `OK` response and each command expected to be rejected prints an `ERROR` response, with the same results in interactive and file mode

### Requirement: Complete help
Every registered command SHALL appear in `HELP` and, with its description, in `HELP EXTENDED`, including the Delivery 3 commands.

#### Scenario: New commands listed
- **WHEN** `HELP` and `HELP EXTENDED` are processed
- **THEN** both list `INVOICE CREATE`, `SPEEDPASS CREATE`, `SPEEDPASS DELETE`, `SPECIALPASS CREATE`, `SPECIALPASS DELETE`, `HELP EXTENDED` and the typed `ATTRACTION CREATE` and `ATTRACTION ENTER …;SPEED` forms

### Requirement: Persistence with Delivery 3 data
Normal execution SHALL persist and restore the Delivery 3 state (hashed passwords, passes with consumption status, attraction types), and `--fly` execution SHALL handle the same state only in its temporary directory, leaving the permanent data unchanged and removing the directory at the end.

#### Scenario: Restart after the script
- **WHEN** the script runs in normal mode and the application restarts
- **THEN** users authenticate with their passwords, passes keep their consumption status and attractions their types

#### Scenario: Script in fly mode
- **WHEN** the script runs with `--fly`
- **THEN** it produces the same responses and the permanent data files are not created or modified

### Requirement: Model matches the final code
`uml/model.puml` SHALL be valid PlantUML and correspond to the final code.

#### Scenario: Model check
- **WHEN** the model is validated with PlantUML and its associations compared with the source fields
- **THEN** it is valid and every association matches a field
