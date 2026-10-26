# Spec Delta

## Purpose

Guarantees that the complete application — all Delivery 1, 2 and 3 commands, layers, controllers and repositories — works together in every execution mode and from the packaged JAR.

## ADDED Requirements

### Requirement: All commands work together
Every E1, E2 and E3 command SHALL be available and SHALL work in combination through the single command-processing system, with consistent state across users, groups, tickets, passes, attractions and invoices.

#### Scenario: Reference scenario
- **WHEN** `examples/input_fase3.txt` is executed
- **THEN** every command in it produces its expected `OK` or `ERROR` response and the final invoices and attraction statuses reflect all previous operations

### Requirement: Execution modes retained
The complete application SHALL run interactively, from a command file given as first non-option argument, and with `--fly` combined with either, using the same processing.

#### Scenario: Same script in every mode
- **WHEN** the reference script is run from the file, through standard input, and with `--fly`
- **THEN** the outputs are identical

### Requirement: Normal and temporary persistence with Delivery 3 data
In normal mode all Delivery 3 state (users, groups, tickets, group tickets, passes with status, attractions with type) SHALL survive a restart; with `--fly` none of it SHALL be written to the permanent data directory.

#### Scenario: Restart after the script
- **WHEN** the reference script ran in normal mode and the application restarts
- **THEN** the invoices and attraction definitions produced afterwards reflect the stored state

### Requirement: Help covers every command
Every command family accepted by the command processor SHALL appear in both `HELP` and `HELP EXTENDED`.

#### Scenario: Help coverage
- **WHEN** the help outputs are compared with the commands handled by the processor
- **THEN** no handled command is missing from either help mode

### Requirement: Packaged application
`mvn package` SHALL produce `target/parque.jar`, which runs the reference script with `java -jar target/parque.jar examples/input_fase3.txt` and with `--fly`.

#### Scenario: JAR run
- **WHEN** `java -jar target/parque.jar --fly examples/input_fase3.txt` is executed after `mvn package`
- **THEN** it processes the script and leaves no temporary directory behind
