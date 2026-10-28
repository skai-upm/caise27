# Spec Delta

## ADDED Requirements

### Requirement: Unexpected failures are contained
If processing a command fails for any unforeseen reason, the application SHALL print an `ERROR:` line for that command and continue with the next command instead of terminating.

#### Scenario: Internal failure during a command
- **WHEN** an unexpected runtime failure occurs while a command is being processed
- **THEN** an `ERROR:` line is printed for that command and the following commands are processed normally
