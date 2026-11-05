## ADDED Requirements

### Requirement: Controlled handling of processing failures
If processing a command fails for any reason other than a normal rejection (for example an internal or input/output error), the application SHALL print an error message starting with `ERROR`, SHALL NOT terminate unexpectedly, and SHALL continue with the next command.

#### Scenario: Failure while saving does not stop the application
- **GIVEN** the users file cannot be written
- **WHEN** a valid `USER CREATE` is processed and then `HELP` is processed
- **THEN** an error message starting with `ERROR` is printed for the creation and the help text is printed for `HELP`

#### Scenario: Assorted malformed input
- **WHEN** the lines `;;;`, `USER`, `user create;a`, `USER UPDATE;;;;` and `EXIT;x` are processed
- **THEN** each prints an error message starting with `ERROR` and the application keeps running
