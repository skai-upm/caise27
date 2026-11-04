# delivery-integration Specification

## Purpose
Guarantees that all Delivery 1, 2 and 3 features work together through the final command system, layers, controllers and repositories, in every execution mode, verified by a repeatable end-to-end scenario.

## Requirements

### Requirement: End-to-end scenario across all deliveries
The project SHALL include a repeatable end-to-end command scenario exercising user, group, date, price, individual and group ticket, attraction (all types), invoice, pass (purchase, SPEED and SPECIAL entry, refund) and help commands, and an automated test that runs it through the shared command processing and checks its results.

#### Scenario: Scenario passes in file mode
- **WHEN** the end-to-end scenario file is run as a command file
- **THEN** every command produces its expected `OK`/`ERROR` result, including the exact ticket, group-ticket and pass purchase replies

### Requirement: All execution modes with the final configuration
Interactive, command-file and `--fly` execution SHALL work with the final repository and controller configuration and SHALL produce the same results for the same commands; normal execution SHALL persist all Delivery 3 state and `--fly` execution SHALL leave the permanent files untouched.

#### Scenario: Same results in every mode
- **WHEN** the end-to-end scenario is run interactively, from the file and with `--fly`
- **THEN** the printed results are identical in the three runs

#### Scenario: Persistence of final state
- **GIVEN** the scenario was run in normal mode
- **WHEN** the application starts again
- **THEN** users, groups, tickets, group tickets, typed attractions and passes (with consumed states) are restored, attractions are empty, and `--fly` runs did not change the permanent files

### Requirement: Executable artifact and documentation consistency
`mvn package` SHALL produce the executable JAR that runs the scenario outside the IDE with `java -jar`, `HELP` and `HELP EXTENDED` SHALL list every command the processor accepts, and `uml/model.puml` SHALL declare exactly the production classes.

#### Scenario: Help lists every accepted command
- **WHEN** each command operation accepted by the processor is compared with the `HELP` and `HELP EXTENDED` output
- **THEN** every operation appears in both

#### Scenario: Packaged JAR outside the IDE
- **WHEN** `java -jar target/amusement-park-1.0-SNAPSHOT.jar <scenario file>` is run from a clean directory
- **THEN** it completes with exit status 0 and the same results as the automated test
