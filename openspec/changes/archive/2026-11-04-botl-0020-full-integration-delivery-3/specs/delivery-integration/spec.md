# Spec Delta

## Purpose

States the end-to-end guarantees of the final delivery: all commands of Deliveries 1–3 work together through the shared command processing, in every execution mode, with permanent and temporary persistence, packaged as an executable JAR and documented by an up-to-date UML model.

## ADDED Requirements

### Requirement: Executable release
`mvn package` SHALL build an executable JAR that runs with `java -jar` without additional classpath configuration.

#### Scenario: Release build
- **WHEN** `mvn clean package` is run and the JAR is executed with a command file in an empty directory
- **THEN** the build succeeds, the commands are processed, and the exit status is 0

### Requirement: All commands available together
All E1, E2 and F3 commands SHALL be processed by the same command processing and work together in one execution without losing earlier functionality.

#### Scenario: End-to-end script
- **GIVEN** `samples/input_fase3.txt` with representative E1, E2 and F3 commands
- **WHEN** it is executed as a command file
- **THEN** every command produces its expected `OK`/`ERROR:` response, the final state files are consistent, and the run ends at `EXIT`

### Requirement: Execution modes retained
Interactive (standard input), command-file and `--fly` executions SHALL work with the final controller and repository configuration, giving the same responses for the same commands.

#### Scenario: Same responses in every mode
- **WHEN** the end-to-end script is run from a file, from standard input, and from a file with `--fly` on fresh data
- **THEN** the three outputs are identical

#### Scenario: Temporary run leaves permanent data untouched
- **GIVEN** permanent data produced by a normal run
- **WHEN** the script is run again with `--fly`
- **THEN** every permanent file is byte-identical afterwards and no temporary directory remains

### Requirement: Persistence of the final state
A normal execution SHALL persist the final Delivery 3 state so that the next execution restores users (with hashed passwords), groups, tickets, group tickets, passes with their consumed status and attraction types.

#### Scenario: Restart after the script
- **WHEN** the application restarts after the end-to-end script
- **THEN** authentication with the changed password works, consumed passes cannot be refunded, group locks and attraction types still apply, and invoices show the same totals

### Requirement: Help covers every command
`HELP` SHALL list the syntax and `HELP EXTENDED` the syntax and description of every registered command, and every command handled by the command processing SHALL be registered.

#### Scenario: Help coverage
- **WHEN** `HELP` and `HELP EXTENDED` are processed
- **THEN** both list the same commands — including users, groups, tickets, group tickets, passes, attractions, invoices, system, help and exit commands — and each extended entry has a description

### Requirement: UML corresponds to the final code
`uml/model.puml` SHALL be valid PlantUML and correspond to the final production classes and relationships.

#### Scenario: Model check
- **WHEN** the UML consistency tests and the PlantUML syntax check run on the final tree
- **THEN** they pass
