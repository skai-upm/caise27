# delivery-integration Specification

## Purpose

States the end-to-end guarantees of the complete application: every delivered command works together in every execution mode with the final configuration of layers, controllers and repositories.

## Requirements

### Requirement: Executable release
`mvn package` SHALL produce an executable JAR that runs outside the IDE with `java -jar`, including its runtime dependencies.

#### Scenario: Release build
- **GIVEN** the project tree
- **WHEN** `mvn package` runs and the JAR is executed from a clean directory with `HELP`
- **THEN** the build succeeds, all tests pass and the help is printed

### Requirement: All commands work together
All E1, E2 and F3 commands SHALL be usable in one session through the shared command processing, with their documented responses, in interactive and file execution.

#### Scenario: End-to-end command file
- **GIVEN** `samples/input_fase3.txt` with users, groups, dates, prices, individual and group tickets, passes, typed attractions, SPEED and Special Pass entries, status, invoices, cascading deletions, HELP, HELP EXTENDED and EXIT
- **WHEN** the application processes it in file mode
- **THEN** every command produces its expected response and the same responses are produced in interactive mode

### Requirement: Final persistence in both modes
Normal persistence SHALL store and restore the complete final state (users with hashed passwords, groups, tickets, group tickets, typed attractions, passes with consumed status), and `--fly` SHALL process the same commands while leaving the permanent data unchanged and deleting its temporary directory.

#### Scenario: Restart after the end-to-end file
- **WHEN** the application restarts after the end-to-end file
- **THEN** users, groups, tickets, group tickets, attraction types and pass states are restored

#### Scenario: Temporary run
- **WHEN** the application runs with `--fly samples/input_fase3.txt`
- **THEN** its output equals the normal run on empty data, the permanent files are untouched and no `tmp/` directory remains

### Requirement: Help and model coverage
`HELP` SHALL list the syntax and `HELP EXTENDED` the syntax and description of every final command, every listed command SHALL be recognised, and `uml/model.puml` SHALL correspond to the final code.

#### Scenario: Help coverage
- **WHEN** `HELP` and `HELP EXTENDED` are processed
- **THEN** both list the same 28 commands, the extended help with descriptions

#### Scenario: Model coverage
- **WHEN** the diagram is compared with the production classes
- **THEN** every class, interface and enum and every model-typed field is represented
