## ADDED Requirements

### Requirement: Temporary persistence mode
When `--fly` is the first argument, optionally followed by a command file path, the application SHALL create a new directory `tmp/<yyyyMMdd_HHmmss>/` (made unique if it already exists), make all repositories use files only inside it, and delete it with all its files when the execution ends normally (EXIT, end of standard input or end of the command file). Both interactive and file mode SHALL be supported with `--fly`; persistent files outside the temporary directory MUST NOT be read or modified. Without `--fly`, argument handling is unchanged.

#### Scenario: Interactive fly mode leaves persistent data untouched
- **GIVEN** a `users.txt` containing Ana in the working directory
- **WHEN** `java -jar target/amusement-park.jar --fly` is run with `USER CREATE` for Pedro and `EXIT`
- **THEN** `USER CREATED` is printed, `users.txt` still contains only Ana, and no `tmp/<timestamp>` directory remains

#### Scenario: Fly mode starts from an empty state
- **GIVEN** a `users.txt` containing Ana in the working directory
- **WHEN** `java -jar target/amusement-park.jar --fly input.txt` is run where `input.txt` creates Ana
- **THEN** `USER CREATED` is printed (Ana is not loaded from the permanent files) and the temporary directory is deleted at the end

#### Scenario: Normal forms unchanged
- **WHEN** the application is run with no arguments or with only a command file
- **THEN** it reads and writes the permanent files in the working directory as before
