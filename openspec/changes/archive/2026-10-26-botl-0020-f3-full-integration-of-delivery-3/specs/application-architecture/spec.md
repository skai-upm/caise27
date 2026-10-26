# Spec Delta

## ADDED Requirements

### Requirement: Integrated Delivery 3
The application SHALL process E1, E2 and E3 commands together through the same command processor, controllers and repositories in interactive mode, command-file mode and `--fly` mode. With normal persistence all data (users, groups, tickets, group tickets, passes, attractions with their types) SHALL be restored after a restart; with `--fly` nothing SHALL be written to the permanent data directory.

#### Scenario: Same script in every mode
- **WHEN** a script creating users, a group, tickets, a group ticket, passes, typed attractions, accesses, an invoice and a refund is run interactively, from a file and with `--fly`
- **THEN** the three executions print the same responses

#### Scenario: Restart with normal persistence
- **WHEN** the script is run with normal persistence and the application is restarted
- **THEN** the stored users, groups, tickets, passes and attraction types are available to the new execution

#### Scenario: Packaged JAR
- **WHEN** `mvn package` is run and the JAR executes the script from a file
- **THEN** it prints the same responses as the in-process execution
