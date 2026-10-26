# Spec Delta

## MODIFIED Requirements

### Requirement: Terminal command input
When started without arguments, the application SHALL run in interactive mode: it SHALL read commands as text lines from standard input and SHALL process each non-blank line as one command, printing the response for each, until it processes `EXIT` or standard input ends.

#### Scenario: Commands processed line by line
- **WHEN** the application is started and receives two valid command lines on standard input
- **THEN** it processes them in order and prints one response for each

#### Scenario: End of input
- **WHEN** standard input reaches its end
- **THEN** the application terminates normally

#### Scenario: Interactive EXIT
- **WHEN** the lines `HELP` and `EXIT` are entered followed by further lines
- **THEN** the application prints the help, terminates after `EXIT`, and reads no further commands

### Requirement: Successful command response
A command that completes successfully SHALL print a response whose first line starts with `OK`.

#### Scenario: OK response
- **WHEN** a valid `USER CREATE` command is processed
- **THEN** the application prints `OK USER CREATED`

#### Scenario: Multi-line response
- **WHEN** `HELP` is processed
- **THEN** the first line of the response starts with `OK`

## ADDED Requirements

### Requirement: Execution mode selection
The execution mode SHALL depend on the program arguments: with no arguments the application SHALL use interactive mode on standard input; with at least one argument the first argument SHALL be interpreted as the path of a command file.

#### Scenario: No arguments
- **WHEN** the application is started without arguments
- **THEN** it reads commands from standard input

#### Scenario: File argument
- **WHEN** the application is started with the argument `input_basico.txt`
- **THEN** it reads commands from that file and not from standard input

### Requirement: File command input
In file mode the application SHALL open the file, read it line by line, and execute each non-blank line as a command in the order in which the lines appear, printing each response, until it processes `EXIT` or reaches the end of the file.

#### Scenario: Commands executed in order
- **WHEN** a file contains two `USER CREATE` lines followed by a `USER UPDATE` line for the first user
- **THEN** both users are created and then the first user is updated, with one response printed per line

### Requirement: Shared command processing
Interactive mode and file mode SHALL use the same command-processing mechanism, so the same command yields the same result whether it comes from the terminal or from a file.

#### Scenario: Same result in both modes
- **WHEN** the same sequence of commands is run once interactively and once from a file, each starting from the same stored data
- **THEN** both runs print identical responses and leave identical stored data

### Requirement: HELP command
The command `HELP` SHALL display the commands currently available and their basic syntax, including at least `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`, `USER DELETE;Correo;Contraseña`, `USER UPDATE;Correo;Contraseña;Campo;NuevoValor`, `HELP` and `EXIT`. The list SHALL be produced from the commands the application supports so that new commands appear in it.

#### Scenario: Help lists all commands
- **WHEN** `HELP` is processed
- **THEN** the response lists the syntax of USER CREATE, USER DELETE, USER UPDATE, HELP and EXIT

#### Scenario: Help with parameters
- **WHEN** `HELP;x` is processed
- **THEN** an `ERROR` line about the wrong number of parameters is printed

### Requirement: EXIT command
The command `EXIT` SHALL terminate the application in a controlled manner after printing a confirmation starting with `OK`. In file mode, processing SHALL stop at `EXIT` and no later line SHALL be executed.

#### Scenario: EXIT in file
- **WHEN** a file contains `USER CREATE` for Ana, then `EXIT`, then `USER CREATE` for Pedro
- **THEN** Ana is created, the application terminates at `EXIT`, and Pedro is not created

### Requirement: Invalid input file
If the file given as argument does not exist, cannot be opened, or cannot be read, the application SHALL print an `ERROR` message describing the problem and terminate in a controlled manner, without showing an unhandled Java exception.

#### Scenario: Missing file
- **WHEN** the application is started with a path that does not exist
- **THEN** it prints an `ERROR` line naming the file and terminates without a stack trace

#### Scenario: Unreadable file
- **WHEN** the application is started with a path that is a directory or cannot be read
- **THEN** it prints an `ERROR` line and terminates without a stack trace

### Requirement: Blank lines ignored
Blank lines (empty or containing only whitespace) SHALL be ignored in both modes and SHALL NOT produce any response or error.

#### Scenario: Blank lines in a file
- **WHEN** a file contains a `USER CREATE` line, an empty line, `HELP` and `EXIT`
- **THEN** the three commands are processed and the blank line produces no output
