# Spec Delta

## MODIFIED Requirements

### Requirement: Command file input
When started with at least one argument other than a leading `--fly` option, the application SHALL treat the first such argument as the path of a text file containing one command per line, process its commands in the order in which they appear, ignore blank lines, print each command's response to standard output, and terminate normally when the end of the file is reached. Standard input is not read in this mode. With only `--fly`, commands are read from standard input.

#### Scenario: Commands from file processed in order
- **WHEN** the application is started with `input.txt` containing `USER CREATE` for Ana, a blank line, `USER CREATE` for Pedro and `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999`
- **THEN** three responses starting with `OK` are printed in that order
- **AND** Ana's telephone is `699999999`
- **AND** the application terminates with exit status 0

#### Scenario: Errors in the file do not stop processing
- **WHEN** a command file contains an invalid command followed by a valid `USER CREATE`
- **THEN** an `ERROR` response is printed for the invalid command and the following command is still processed

#### Scenario: Command file after --fly
- **WHEN** the application is started with `--fly input.txt`
- **THEN** the commands of `input.txt` are processed in order
