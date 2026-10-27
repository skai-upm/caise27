# Spec Delta

## MODIFIED Requirements

### Requirement: HELP command
The `HELP` command SHALL display the available commands and their basic syntax, including at least: `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `HELP` and `EXIT`, together with the commands of later deliveries listed in their own requirements.

#### Scenario: Help lists the required commands
- **WHEN** the user enters `HELP`
- **THEN** the output contains `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `HELP` and `EXIT`

## ADDED Requirements

### Requirement: Delivery 2 commands coexist
All Delivery 1 and Delivery 2 commands SHALL work together in one session, from standard input or a command file, with and without `--fly`.

#### Scenario: Complete Delivery 2 session from a file
- **WHEN** a command file creates users, sets the system date and price, creates a group with members, buys an individual ticket and a group ticket, creates attractions, enters with both tickets, exits one user, queries status and list, and ends with `EXIT`
- **THEN** every operation succeeds with the expected prices and attraction status, and the same file run with `--fly` produces the same output and leaves no temporary data

#### Scenario: Rejected operations in the session
- **WHEN** the session also tries to delete a ticket for the current date, modify a group locked by an active group ticket, delete a user with an active ticket, and enter an attraction without enough capacity for a group
- **THEN** each is rejected with an error and the session continues
