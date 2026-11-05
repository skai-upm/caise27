## ADDED Requirements

### Requirement: Controlled handling of unreadable users file
If the users file exists but cannot be read at start-up, the application SHALL print an error message starting with `ERROR` that identifies the file and terminate with exit status 1, without an uncaught exception or stack trace.

#### Scenario: Users file is not a readable file
- **GIVEN** `users.txt` in the working directory is a directory
- **WHEN** the application is started
- **THEN** an `ERROR` message mentioning `users.txt` is printed and the exit status is 1
