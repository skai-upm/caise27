# Spec Delta

## MODIFIED Requirements

### Requirement: Interactive mode
When started without a command-file argument (no arguments, or only the `--fly` option), the application SHALL read commands line by line from standard input and process each one, until it receives `EXIT` or standard input ends.

#### Scenario: Interactive session
- **WHEN** the application is started without arguments and receives `HELP` then `EXIT` on standard input
- **THEN** it prints the help text and terminates after `EXIT`

#### Scenario: End of input
- **WHEN** standard input ends without `EXIT`
- **THEN** the application terminates in a controlled manner

### Requirement: File mode
When started with a command-file argument (the first argument, or the argument following the `--fly` option), the application SHALL treat it as the path of a text file containing one command per line, read it line by line and execute the commands in order, using exactly the same command processing as interactive mode.

#### Scenario: Commands from file
- **WHEN** the application is started with a file containing two `USER CREATE` lines and a `USER UPDATE` line
- **THEN** the three commands are executed in order with the same results they would have interactively

### Requirement: HELP command
`HELP` SHALL display all commands currently available and their basic syntax: the USER, SYSTEM, GROUP, TICKET, GROUPTICKET and ATTRACTION commands, `HELP` and `EXIT`.

#### Scenario: Help output
- **WHEN** `HELP` is processed
- **THEN** the output lists the syntax of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `SYSTEM DATE`, `SYSTEM PRICE`, `GROUP CREATE`, `GROUP ADD`, `GROUP REMOVE`, `GROUP DELETE`, `TICKET CREATE`, `TICKET DELETE`, `GROUPTICKET CREATE`, `GROUPTICKET DELETE`, `ATTRACTION CREATE`, `ATTRACTION DELETE`, `ATTRACTION ENTER`, `ATTRACTION EXIT`, `ATTRACTION STATUS`, `HELP` and `EXIT`
