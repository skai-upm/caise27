# Spec Delta

## MODIFIED Requirements

### Requirement: Temporary data directory with --fly
When `--fly` is among the launch arguments, the application SHALL create at startup a new directory `tmp/<yyyyMMdd_HHmmss>` (relative to the working directory, named with the run's start timestamp and made unique if that name already exists), initially empty, and every persistent element (users, groups, tickets, group tickets, attractions with their type, and passes) SHALL be read from and written to files inside it only.

#### Scenario: Interactive fly run
- **GIVEN** persistent data files exist in the working directory
- **WHEN** the application is started with `--fly` and `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** the user is created in the run's temporary directory and the persistent files in the working directory are byte-for-byte unchanged

#### Scenario: Fly run with a command file
- **WHEN** the application is started with `--fly input_fase2.txt`
- **THEN** the commands of `input_fase2.txt` are processed against the temporary data and the normal persistent data is unchanged

#### Scenario: Independent from normal data
- **GIVEN** a user stored in the normal persistent data
- **WHEN** a `--fly` run starts
- **THEN** that user does not exist in the fly run

#### Scenario: Delivery 3 data isolated
- **GIVEN** normal persistent data including `passes.txt`
- **WHEN** a `--fly` run buys and consumes passes, creates typed attractions and ends
- **THEN** the normal files are byte-for-byte unchanged and the run directory is deleted
