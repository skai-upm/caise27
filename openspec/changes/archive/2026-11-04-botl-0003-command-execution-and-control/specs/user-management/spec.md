# Spec Delta

## MODIFIED Requirements

### Requirement: Command parsing
The application SHALL split each command line into its components with `String.split` on `;`, whether the line comes from the terminal or from a command file. The first component SHALL name the operation (`USER CREATE`, `USER DELETE`, `USER UPDATE`, or a general command such as `HELP` or `EXIT`). Each user command SHALL produce exactly one result line starting with `OK` on success or `ERROR` on failure.

#### Scenario: Unknown operation
- **WHEN** the command `USER RENAME;ana@correo.com` is processed
- **THEN** the result starts with `ERROR`, no user data changes and the application keeps accepting commands

#### Scenario: Wrong number of parameters
- **WHEN** a `USER CREATE`, `USER DELETE` or `USER UPDATE` command has fewer or more parameters than its format requires
- **THEN** the result starts with `ERROR`, no user is created or changed and the application keeps accepting commands

#### Scenario: Blank line
- **WHEN** an empty or blank line is read
- **THEN** it is ignored without changing any data
