# Spec Delta

## MODIFIED Requirements

### Requirement: Interactive mode
When launched without a command-file argument (no arguments, or only the `--fly` option), the application SHALL read commands from standard input, one per line, process each line as soon as it is read, and keep waiting for further lines until `EXIT` is processed or the input ends.

#### Scenario: Interactive session
- **GIVEN** the application started without arguments
- **WHEN** the lines `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`, `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999`, `HELP`, `EXIT` are entered
- **THEN** the user is created, the phone is updated, the help is shown, and the application terminates after `EXIT`

#### Scenario: End of input
- **GIVEN** the application started without arguments
- **WHEN** standard input ends without `EXIT`
- **THEN** the application terminates normally after processing every line received

#### Scenario: Only the fly option
- **WHEN** the application is launched with `--fly` only
- **THEN** it reads commands from standard input

### Requirement: File mode
When launched with a command-file argument, the application SHALL treat the first argument that is not the `--fly` option as the path of a text file, read it line by line and process each non-blank line as a command, in file order.

#### Scenario: Batch file run
- **GIVEN** a file `input_basico.txt` with lines creating Ana and Pedro, updating Ana's phone to `699999999`, then `EXIT`
- **WHEN** the application is launched with `input_basico.txt`
- **THEN** both users exist and Ana's phone is `699999999`

#### Scenario: Blank lines are skipped
- **GIVEN** a file with an empty line between a valid `USER CREATE` and `HELP`
- **WHEN** it is processed
- **THEN** the empty line produces no output and no error, and both commands are executed

#### Scenario: File after the fly option
- **WHEN** the application is launched with `--fly input_fase2.txt`
- **THEN** `input_fase2.txt` is processed as the command file
