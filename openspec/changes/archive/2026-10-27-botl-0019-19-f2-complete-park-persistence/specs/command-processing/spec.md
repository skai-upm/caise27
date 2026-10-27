# Spec Delta

## ADDED Requirements

### Requirement: Execution forms with temporary mode
The application SHALL support the execution forms `java -jar target/amusement-park.jar`, `java -jar target/amusement-park.jar input_fase2.txt`, `java -jar target/amusement-park.jar --fly` and `java -jar target/amusement-park.jar --fly input_fase2.txt`. `--fly` SHALL only be recognised as the first argument; without it, argument handling SHALL remain as in Delivery 1.

#### Scenario: Fly with command file
- **WHEN** the application is started with `--fly input_fase2.txt`
- **THEN** the commands of `input_fase2.txt` are processed in temporary mode

#### Scenario: Fly interactively
- **WHEN** the application is started with `--fly` only
- **THEN** commands are read from standard input in temporary mode

#### Scenario: Normal file mode unchanged
- **WHEN** the application is started with `input_fase2.txt` only
- **THEN** the commands are processed with normal persistence
