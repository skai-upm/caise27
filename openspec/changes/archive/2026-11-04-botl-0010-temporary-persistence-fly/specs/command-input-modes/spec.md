# Spec Delta

## MODIFIED Requirements

### Requirement: Interactive mode
When started without a command-file argument (no arguments, or only `--fly`), the application SHALL read commands from standard input, one per line, process each one, and wait for the next until it processes `EXIT` or standard input ends.

#### Scenario: Interactive session
- **WHEN** the application is started without arguments and receives `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`, `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999`, `HELP` and `EXIT`
- **THEN** the user is created, the phone is updated, the help is shown and the application terminates after `EXIT`

#### Scenario: End of standard input
- **WHEN** standard input ends without an `EXIT` command
- **THEN** the application terminates normally after processing all received commands

#### Scenario: Interactive with --fly
- **WHEN** the application is started with only `--fly`
- **THEN** it reads commands from standard input

### Requirement: File mode
When started with a command-file argument, the application SHALL treat that argument as the path of a text file, read it line by line, and process each non-blank line as a command in the order the lines appear, until it processes `EXIT` or the file ends. The command-file argument is the first argument, or the second one when the first is `--fly`. Blank lines SHALL be skipped without error.

#### Scenario: File with commands in order
- **WHEN** the application is started with a file containing a creation of Ana, a creation of Pedro, `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` and `EXIT`
- **THEN** both users exist and Ana's phone is `699999999`

#### Scenario: Blank lines in the file
- **WHEN** the file contains empty or whitespace-only lines between valid commands
- **THEN** those lines are skipped and the valid commands are executed

#### Scenario: File after --fly
- **WHEN** the application is started with `--fly input_fase2.txt`
- **THEN** `input_fase2.txt` is processed as the command file
