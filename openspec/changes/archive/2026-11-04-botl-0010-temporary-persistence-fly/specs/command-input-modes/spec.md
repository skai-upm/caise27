## MODIFIED Requirements

### Requirement: File mode with one argument
When started with at least one argument other than `--fly`, the application SHALL interpret the first such argument as the path of a text file, read it line by line and process each line as a command in the order it appears; `--fly` alone selects interactive mode.

#### Scenario: Commands executed in file order
- **GIVEN** a file with the lines `USER CREATE;Ana;…`, `USER CREATE;Pedro;…`, `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999`, `EXIT`
- **WHEN** the application is started with that file path as first argument
- **THEN** both users are created, Ana's phone is `699999999`, and the application terminates

#### Scenario: File after --fly
- **WHEN** the application is started with `--fly input_fase2.txt`
- **THEN** `input_fase2.txt` is read as the command file

#### Scenario: Only --fly
- **WHEN** the application is started with only `--fly`
- **THEN** commands are read from standard input
