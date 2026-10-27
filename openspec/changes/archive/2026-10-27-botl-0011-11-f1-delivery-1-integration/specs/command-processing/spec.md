# Spec Delta

## ADDED Requirements

### Requirement: Unexpected failures contained
Any unexpected failure while executing a single command SHALL be reported as an error message for that command and SHALL NOT terminate the application; subsequent commands SHALL still be processed.

#### Scenario: Failure inside a command
- **WHEN** executing a command raises an unforeseen runtime failure
- **THEN** an error message is written for that command and the next command is processed normally
