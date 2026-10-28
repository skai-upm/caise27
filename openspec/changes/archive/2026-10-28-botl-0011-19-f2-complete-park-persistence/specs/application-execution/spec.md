## ADDED Requirements

### Requirement: Temporary persistence mode
When `--fly` is the first argument, the application SHALL create an independent temporary directory `tmp/<yyyyMMdd_HHmmss>/`, make every repository use only files inside it, run interactively or with the command file given as optional second argument, and delete the directory and all its files when execution ends through EXIT or when the input ends. The repositories SHALL be the same as in normal mode, differing only in configured paths. Without `--fly`, argument handling SHALL remain unchanged.

#### Scenario: Fly mode with a command file
- **WHEN** the JAR is run with `--fly input.txt` and the file creates attractions and ends with `EXIT`
- **THEN** the commands are executed, the permanent data files are not modified, and no `tmp/<timestamp>/` directory remains afterwards

#### Scenario: Fly mode interactively
- **WHEN** the JAR is run with `--fly` and receives `EXIT` on standard input
- **THEN** the temporary directory created at start-up no longer exists after the application ends

#### Scenario: Fly mode isolates from permanent data
- **WHEN** permanent data contains attraction `A01` and the JAR is run with `--fly` processing `ATTRACTION LIST;A01`
- **THEN** an error message reports that the attraction does not exist
