# Spec Delta

## MODIFIED Requirements

### Requirement: Interactive command input
When started without arguments, or with `--fly` as its only argument, after printing its start banner the application SHALL read commands from standard input, one command per line, process each line in order and print the resulting message, waiting for the next command until standard input ends. Blank lines MUST be ignored.

#### Scenario: Commands processed in order
- **GIVEN** the application is started with two valid `USER CREATE` lines on standard input
- **WHEN** the input is processed
- **THEN** one result message is printed for each line, in input order

#### Scenario: End of input stops reading
- **GIVEN** the application is started with empty standard input
- **WHEN** it runs
- **THEN** it prints the start banner and finishes normally

### Requirement: Command file input
When started with a first argument other than `--fly`, the application SHALL treat it as the path of a text file with one command per line; when the first argument is `--fly`, the second argument, if present, SHALL be that path. The commands are processed in the order they appear, blank lines are ignored and standard input is not read.

#### Scenario: Commands processed in file order
- **GIVEN** a file with the lines `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`, an empty line, and `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999`
- **WHEN** `java -jar target/amusement-park.jar <file>` is run
- **THEN** the creation result is printed before the update result, and the stored user's phone is `699999999`

#### Scenario: Command file after --fly
- **WHEN** `java -jar target/amusement-park.jar --fly <file>` is run
- **THEN** the file's commands are processed in order exactly as without `--fly`
