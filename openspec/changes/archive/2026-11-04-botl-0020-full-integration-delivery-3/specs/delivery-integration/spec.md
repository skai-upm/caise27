# Spec Delta

## Purpose

Defines the end-to-end checks that show all Delivery 1, 2 and 3 features working together in the packaged application, in every execution mode.

## ADDED Requirements

### Requirement: End-to-end command sequence
A single command file containing representative E1, E2 and F3 commands (users, groups, date and price, individual and group tickets, typed attractions with normal, SPEED and Special Pass entries, passes, invoices, cascade deletion, both help modes and EXIT) SHALL be processed by the application through the shared command processing with every command producing its specified result, and the same file SHALL produce the same output when supplied on standard input.

#### Scenario: Integrated run
- **GIVEN** the end-to-end command file of the project
- **WHEN** the application processes it in file mode and again in interactive mode from empty data
- **THEN** both runs print identical output in which every command reports its expected `OK` or `ERROR` result

### Requirement: Temporary integrated run
Processing the end-to-end command file with `--fly` SHALL produce the same output as a normal run from empty data, SHALL leave existing permanent data files unchanged, and SHALL leave no temporary run directory behind.

#### Scenario: --fly with the end-to-end file
- **GIVEN** permanent data files exist
- **WHEN** the application runs `--fly` with the end-to-end command file
- **THEN** the output equals that of a normal run on empty data, the permanent files are byte-for-byte unchanged and `tmp/` has no run directory

### Requirement: Help covers every command
`HELP` SHALL list the syntax, and `HELP EXTENDED` the syntax and description, of every command form the application accepts, including all Delivery 3 commands.

#### Scenario: Help coverage
- **WHEN** `HELP` and `HELP EXTENDED` are processed
- **THEN** each syntax of the command registry appears in both outputs, and the registry matches the operations the processor accepts

### Requirement: Release build
`mvn package` SHALL run the whole regression suite successfully and produce one executable JAR, including its runtime dependencies, that runs the end-to-end command file with `java -jar` outside the build tool.

#### Scenario: Packaged run
- **WHEN** `mvn package` completes and `java -jar target/amusement-park-1.0-SNAPSHOT.jar <end-to-end file>` is run in an empty directory
- **THEN** the build reports zero test failures and the JAR's output equals the expected end-to-end output
