# Spec Delta

## ADDED Requirements

### Requirement: Temporary persistence mode
When the first argument is `--fly`, optionally followed by a command file path, the application SHALL create a new directory `tmp/<yyyyMMdd_HHmmss>/` and SHALL make every repository read and write only inside it, using the same repositories configured with different paths. When the run ends, whether through `EXIT` or after the command file or standard input is processed normally, the directory and all its files SHALL be deleted. Without `--fly`, arguments SHALL behave exactly as before and the normal persistent data SHALL be used.

#### Scenario: Fly mode with a command file
- **GIVEN** normal persistent data containing group `G01`
- **WHEN** `java -jar target/amusement-park.jar --fly input.txt` runs a file that creates group `G09` and ends with `EXIT`
- **THEN** `G01` was not visible during the run, and afterwards the normal data does not contain `G09` and the created `tmp/<timestamp>/` directory no longer exists

#### Scenario: Fly mode interactively
- **WHEN** `java -jar target/amusement-park.jar --fly` is run with commands on standard input ending in `EXIT`
- **THEN** the commands are processed against an empty temporary state and the temporary directory is deleted at the end

#### Scenario: Fly mode cleaned up at end of file without EXIT
- **WHEN** `--fly input.txt` processes a file without `EXIT`
- **THEN** the temporary directory is deleted after the last line

#### Scenario: Normal mode unchanged
- **WHEN** the JAR runs without `--fly`, either interactively or with a command file
- **THEN** the persistent data directory is used, and it is kept after the run ends
