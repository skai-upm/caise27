# Spec Delta

## ADDED Requirements

### Requirement: Extended help
`HELP EXTENDED` SHALL display, for every command recognised by the application, its syntax together with a brief explanation of its purpose. `HELP` SHALL remain available and keep displaying the syntax of the recognised commands, including `HELP EXTENDED`.

#### Scenario: Extended help content
- **WHEN** the user enters `HELP EXTENDED`
- **THEN** every command syntax shown by `HELP` appears with a description of its purpose (for example `TICKET CREATE;<identifier>;<email>;<date>` with an explanation that it buys an individual ticket)

#### Scenario: Plain help unchanged
- **WHEN** the user enters `HELP`
- **THEN** the syntax of every command is shown without descriptions, including `HELP EXTENDED`

#### Scenario: Same behaviour from a file
- **WHEN** `HELP EXTENDED` is read from a command file
- **THEN** the same extended help is displayed and processing continues

### Requirement: Injected help generation behaviour
The component that generates help text SHALL receive, through dependency injection, the behaviour used to build its output, so that new help formats can be added without changing the logic that reads and executes commands.

#### Scenario: Alternative format injected
- **WHEN** a help generator is created with a different formatting behaviour
- **THEN** it produces the help in that format from the same command information, with no change to command processing
