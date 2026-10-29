# Spec Delta

## ADDED Requirements

### Requirement: Controlled handling of unprocessable input
Any command line that cannot be processed correctly SHALL be reported with an error message, SHALL NOT terminate the application, and SHALL NOT prevent the following lines from being processed. A command file that does not exist or cannot be read SHALL be reported with an error message instead of an uncontrolled failure.

#### Scenario: Unexpected arguments for a general command
- **WHEN** the line `HELP;extra` is processed, followed by `HELP`
- **THEN** an error message is shown for the first line and the help text is shown for the second

#### Scenario: Internal failure while processing a line
- **WHEN** processing a line raises an unexpected error
- **THEN** an error message is shown and the next line is processed

#### Scenario: Nonexistent command file
- **WHEN** the application is started with the path of a file that does not exist
- **THEN** an error message naming the file is shown and the application terminates without a stack trace

### Requirement: Command fields separated by semicolons
Command lines SHALL be split into fields on the `;` character using basic string operations, the first field being the command keyword and the remaining fields its arguments.

#### Scenario: Keyword taken from first field
- **WHEN** the line `EXIT` is processed
- **THEN** it is recognised as the EXIT command with no arguments

#### Scenario: Surrounding whitespace ignored
- **WHEN** the line `  HELP  ` is processed
- **THEN** it is recognised as the HELP command
