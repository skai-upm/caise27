# execution-modes Specification

## Purpose
Defines how the Amusement Park application is launched from the terminal and where it reads its commands from: interactively from standard input, or from a text file given as argument.

## Requirements

### Requirement: Interactive mode
When started with `java -jar` and no arguments, the application SHALL start in interactive mode, reading commands line by line from standard input and processing each line until standard input ends.

#### Scenario: Start without arguments
- **WHEN** the user runs `java -jar target/amusement-park.jar` and then closes standard input
- **THEN** the application waits for and reads commands from standard input and terminates normally once input ends

### Requirement: File mode
When started with `java -jar` and one argument, the application SHALL treat the argument as the path of a text file and process the commands contained in it, line by line, in order.

#### Scenario: Start with an input file
- **WHEN** the user runs `java -jar target/amusement-park.jar input.txt` with an existing readable `input.txt`
- **THEN** the application processes every line of `input.txt` in order and terminates normally without reading standard input

#### Scenario: Input file cannot be read
- **WHEN** the user runs `java -jar target/amusement-park.jar missing.txt` and `missing.txt` does not exist
- **THEN** the application writes an error message naming the file to standard error and terminates with a non-zero exit status
