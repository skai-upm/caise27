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
- **WHEN** a line whose command name is not part of the supported command set (e.g. `USER PROMOTE;ana@correo.com`) is entered
- **THEN** an error response starting with `ERROR` is printed and the application keeps running

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

### Requirement: Same behavior for every command source
Commands read from a file and commands read from standard input SHALL be processed by the same command-processing logic and SHALL produce identical responses and state changes for identical input lines.

#### Scenario: Identical output for both sources
- **WHEN** the same sequence of command lines is given once through standard input and once as a command file, each starting from the same stored users
- **THEN** the printed responses are identical and the resulting stored users are identical

### Requirement: Unreadable command file handled
If the command file given as the first argument does not exist or cannot be read, the application SHALL print an error message starting with `ERROR` that names the file, MUST NOT terminate with an uncaught exception, and MUST NOT execute any command or modify stored users.

#### Scenario: Nonexistent file
- **WHEN** the application is started with the argument `missing.txt` and no such file exists
- **THEN** an `ERROR` message mentioning `missing.txt` is printed
- **AND** no stack trace is printed and the stored users are unchanged

#### Scenario: Unreadable path
- **WHEN** the argument names a directory instead of a regular file
- **THEN** an `ERROR` message mentioning the path is printed and the application terminates without an uncaught exception
