# Spec Delta

## MODIFIED Requirements

### Requirement: Temporary data directory
When launched with the `--fly` option, the application SHALL create at startup a new directory `tmp/<yyyyMMdd_HHmmss>/`, named after the start timestamp and distinct for every execution, and every persistent element (users, groups, tickets, group tickets, attractions with their type, and passes with their consumed status) SHALL be read from and written to files in that directory only. The run SHALL start with no stored data.

#### Scenario: Isolated interactive run
- **GIVEN** permanent data files with existing users
- **WHEN** the application runs with `--fly` and creates a user, a group and a ticket
- **THEN** the changes are written under `tmp/<timestamp>/` and the permanent files are byte-for-byte unchanged

#### Scenario: Isolated file run
- **GIVEN** a command file `input_fase2.txt`
- **WHEN** the application runs with `--fly input_fase2.txt`
- **THEN** its commands operate on the temporary data, exactly as they would in normal file mode

#### Scenario: Distinct directory per execution
- **GIVEN** a temporary directory of a previous run still exists with the same timestamp
- **WHEN** a new `--fly` run starts
- **THEN** it uses a different, new directory

#### Scenario: Delivery 3 data isolated
- **GIVEN** permanent data files
- **WHEN** a `--fly` run creates typed attractions, buys and consumes passes, and ends
- **THEN** the permanent files are unchanged and the temporary directory with its `passes.txt` and `attractions.txt` is deleted
