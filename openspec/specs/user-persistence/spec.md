# user-persistence Specification

## Purpose

Keeps registered users between application runs by storing them in a `;`-separated text file and loading it at start-up.

## Requirements

### Requirement: User file format
Users SHALL be stored in the text file `users.txt` in the working directory, one user per line, with attributes separated by `;` in the order FirstName;Surnames;DNI;Email;Password;Phone;Card.

#### Scenario: File content after creating a user
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` succeeds
- **THEN** `users.txt` contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

### Requirement: Users preserved between runs
Every change to the set of users SHALL be written to the file, and the users stored in it SHALL be loaded when the application starts.

#### Scenario: User available after restart
- **WHEN** the application creates user `12345678Z` and terminates, and is started again
- **THEN** a new `USER CREATE` with DNI `12345678Z` is rejected as a duplicate

#### Scenario: No file yet
- **WHEN** the application starts and `users.txt` does not exist
- **THEN** it starts with no users and without error

### Requirement: Malformed stored lines tolerated
Lines of the user file that do not contain exactly seven `;`-separated fields SHALL be ignored on load without terminating the application.

#### Scenario: Corrupt line skipped
- **WHEN** `users.txt` contains a valid user line and the line `garbage`
- **THEN** the application starts with the valid user loaded

### Requirement: Persistence separated from user and main classes
File reading and writing SHALL be implemented in a class separate from the user class and the main class, using only the Java standard library (no databases or persistence frameworks).

#### Scenario: Responsibility inspection
- **WHEN** the user class and the main class are inspected
- **THEN** neither reads or writes the user file contents
