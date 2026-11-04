# Spec Delta

## MODIFIED Requirements

### Requirement: Interactive mode
When launched without a command-file argument (no arguments, or only the `--fly` option), the application SHALL read commands from standard input, one command per line, and after each non-EXIT command SHALL wait for the next line until `EXIT` is processed or standard input ends.

#### Scenario: Interactive session
- **GIVEN** the application starts without arguments
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`, `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999`, `HELP` and `EXIT` are entered
- **THEN** the create, update and help results are printed, the application stops after `EXIT` and Ana's phone is persisted as `699999999`

#### Scenario: End of input without EXIT
- **GIVEN** interactive mode
- **WHEN** standard input ends after a valid command
- **THEN** the command is processed and the application terminates normally

#### Scenario: Interactive with --fly
- **WHEN** the application is launched with only `--fly`
- **THEN** it runs in interactive mode on temporary data

### Requirement: File mode
When launched with an argument other than the `--fly` option, the application SHALL treat the first such argument as the path of a text command file, read it line by line and execute each line as a command in the order it appears.

#### Scenario: Basic input file
- **GIVEN** a file `input_basico.txt` containing the creation of Ana and Pedro, `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` and `EXIT`
- **WHEN** the application is launched with `input_basico.txt` as first argument
- **THEN** both users are created, Ana's phone becomes `699999999`, and processing ends at `EXIT`

#### Scenario: File mode with --fly
- **WHEN** the application is launched with `--fly input_fase2.txt`
- **THEN** `input_fase2.txt` is processed as the command file on temporary data
