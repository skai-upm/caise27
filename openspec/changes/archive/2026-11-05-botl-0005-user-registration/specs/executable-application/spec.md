## MODIFIED Requirements

### Requirement: Interactive mode on standard input
When launched without arguments, the application SHALL start in interactive mode, waiting for commands read line by line from standard input, SHALL process each line read as a command and print its response to standard output, and SHALL terminate normally (exit status 0) when standard input reaches its end.

#### Scenario: Application waits for input
- **GIVEN** the application launched with `java -jar target/amusement-park.jar` and no arguments
- **WHEN** no input has been provided yet
- **THEN** the process remains running, waiting to read from standard input

#### Scenario: End of input terminates normally
- **GIVEN** the application running in interactive mode
- **WHEN** standard input is closed (end of input)
- **THEN** the application terminates with exit status 0

#### Scenario: Lines are processed as commands
- **GIVEN** the application running in interactive mode
- **WHEN** a valid `USER CREATE` line is typed
- **THEN** the response `USER CREATED` is printed to standard output before the next line is read
