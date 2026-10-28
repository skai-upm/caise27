# Spec Delta

## MODIFIED Requirements

### Requirement: Interactive mode
When started with `java -jar` and no arguments, the application SHALL start in interactive mode, reading commands line by line from standard input and processing each line until standard input ends. The response to each command SHALL be printed before the application waits for the next command.

#### Scenario: Start without arguments
- **WHEN** the user runs `java -jar target/amusement-park.jar` and then closes standard input
- **THEN** the application waits for and reads commands from standard input and terminates normally once input ends

#### Scenario: Response before next command
- **WHEN** a command is typed in interactive mode
- **THEN** its response is printed immediately and the application keeps waiting for the next command

### Requirement: File mode
When started with `java -jar` and at least one argument, the application SHALL treat the first argument as the path of a text file containing one command per line and process those commands in the order they appear, ignoring blank lines.

#### Scenario: Start with an input file
- **WHEN** the user runs `java -jar target/amusement-park.jar input.txt` with an existing readable `input.txt`
- **THEN** the application processes every line of `input.txt` in order and terminates normally without reading standard input

#### Scenario: Input file cannot be read
- **WHEN** the user runs `java -jar target/amusement-park.jar missing.txt` and `missing.txt` does not exist
- **THEN** the application writes an error message naming the file to standard error and terminates with a non-zero exit status

#### Scenario: Path is not a readable file
- **WHEN** the first argument names a directory or a file that cannot be read
- **THEN** the application writes an error message naming the path to standard error, prints no stack trace, and terminates with a non-zero exit status

## ADDED Requirements

### Requirement: Identical processing for both sources
Commands read from a file and commands read from standard input SHALL be processed by the same command processing, producing the same responses and the same effects for the same sequence of commands.

#### Scenario: Same sequence, same output
- **WHEN** the same sequence of commands (including a valid create, a duplicate, an update and an unknown command) is run once from standard input and once from a file, each time starting from the same stored users
- **THEN** both runs print exactly the same responses and leave the same users stored
