# command-interface Specification

## Purpose
Defines the text command interface of the Amusement Park application: reading commands from standard input, splitting their elements by `;`, dispatching them, and handling incorrect commands without terminating the application.

## Requirements

### Requirement: Interactive command input
When started without arguments, the application SHALL read commands from standard input, one command per line, process each in order, print each command's response to standard output, and terminate normally with exit status 0 when standard input ends.

#### Scenario: Commands processed in order
- **WHEN** the application is started with standard input containing two valid `USER CREATE` lines for different users
- **THEN** both commands are processed in input order and a response is printed for each

#### Scenario: End of input terminates normally
- **WHEN** standard input ends
- **THEN** the application terminates with exit status 0

#### Scenario: Blank lines ignored
- **WHEN** a line is empty or contains only whitespace
- **THEN** it is ignored and produces no response

### Requirement: Command element separation
Command elements SHALL be separated by the `;` character and interpreted using basic String operations including `split`, without external libraries. The command name is the first element, compared exactly (for example `USER CREATE`); surrounding whitespace of each element is ignored.

#### Scenario: Elements split by semicolon
- **WHEN** the line `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is interpreted
- **THEN** the command name is `USER CREATE` and the seven following elements are `Ana`, `Garcia Lopez`, `12345678Z`, `ana@correo.com`, `Clave1!`, `612345678` and `1234567890123456`

### Requirement: Incorrect commands do not terminate the application
An unrecognized, incomplete or otherwise incorrect command SHALL produce an error response starting with `ERROR` and MUST NOT terminate the application or change any stored data; subsequent commands SHALL still be processed.

#### Scenario: Unknown command
- **WHEN** the line `FOO;bar` is entered, followed by a valid `USER CREATE` command
- **THEN** an error response starting with `ERROR` is printed for `FOO;bar`
- **AND** the following `USER CREATE` is still processed and the user is created

#### Scenario: Not-yet-supported command
- **WHEN** a line whose command name is not implemented in this delivery step (e.g. `USER UPDATE;ana@correo.com;Clave1!`) is entered
- **THEN** an error response starting with `ERROR` is printed and the application keeps running
