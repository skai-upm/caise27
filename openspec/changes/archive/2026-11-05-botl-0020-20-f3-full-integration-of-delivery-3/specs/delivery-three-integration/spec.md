# Spec Delta

## Purpose

States the release-level guarantees of Delivery 3: the packaged application runs all E1, E2 and F3 commands together through the shared command processing, in every execution mode, with consistent help, persistence and design documentation.

## ADDED Requirements

### Requirement: Executable release
`mvn package` SHALL run the regression suite successfully and produce an executable JAR that starts with `java -jar`.

#### Scenario: Release verification
- **WHEN** `mvn package` runs on the project tree
- **THEN** all tests pass and `java -jar target/<jar>` processes commands

### Requirement: All commands work together
All E1, E2 and F3 commands SHALL remain available through the shared command processing and work together in one execution.

#### Scenario: End-to-end command file
- **GIVEN** a valid command file using users, groups, tickets, group tickets, passes, typed attractions with SPEED and SPECIAL entry, invoices, SYSTEM DATE/PRICE, HELP, HELP EXTENDED and EXIT
- **WHEN** the application processes it
- **THEN** every command produces its expected success result and the stored data reflects all of them

### Requirement: Execution modes with the final configuration
Interactive, command-file and `--fly` execution SHALL work with the final controller and repository configuration, and `--fly` SHALL leave the normal persistent data unchanged.

#### Scenario: Interactive and file equivalence
- **WHEN** the integration commands are supplied interactively and from a file on empty data
- **THEN** both outputs are identical

#### Scenario: Temporary run
- **GIVEN** existing normal data
- **WHEN** the application runs `--fly` with the integration command file and exits
- **THEN** the normal data files are byte-for-byte unchanged and no run directory remains

### Requirement: Help covers all commands
Every registered command SHALL be recognised by command processing, and `HELP` SHALL show its syntax while `HELP EXTENDED` shows its syntax and description.

#### Scenario: Help coverage
- **WHEN** each registered command keyword is processed and `HELP` and `HELP EXTENDED` are requested
- **THEN** no registered command is reported as unknown and each appears in both help outputs

### Requirement: Model matches final code
`uml/model.puml` SHALL be valid PlantUML corresponding to the final implementation.

#### Scenario: Final model check
- **WHEN** the model is checked with PlantUML and compared with the sources
- **THEN** it parses and every class and relationship corresponds to the code
