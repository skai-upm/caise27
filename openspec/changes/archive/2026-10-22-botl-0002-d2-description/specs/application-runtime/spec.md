# Spec Delta

## ADDED Requirements

### Requirement: Temporary persistence mode
When `--fly` is the first argument, optionally followed by a command file path, the application SHALL create a new directory `tmp/<yyyyMMdd_HHmmss>/` under the working directory, keep every data file (users, groups, tickets, attractions) only there during the execution, and delete that directory with all its files when the execution ends, whether through `EXIT` or the end of input. Without `--fly`, argument handling SHALL be unchanged.

#### Scenario: Fly mode with a command file
- **WHEN** `java -jar target/<app>.jar --fly input.txt` runs a file that creates users and tickets
- **THEN** the commands behave as in normal mode, the working directory's data files are unchanged, and no temporary directory remains afterwards

#### Scenario: Fly mode interactively
- **WHEN** the JAR is run with only `--fly` and the user types `EXIT`
- **THEN** the temporary directory created at startup no longer exists
