# Spec Delta

## Purpose

Defines how users are preserved between runs of the application: the text file format, loading of stored users at startup, and saving whenever users change.

## ADDED Requirements

### Requirement: User file format
Users SHALL be stored in the text file `users.txt` of the working directory, one user per line, with the attributes first name, surnames, DNI, email, password, phone and card number in that order separated by `;`.

#### Scenario: Stored line for a user
- **WHEN** the user Ana Garcia Lopez with DNI `12345678Z`, email `ana@correo.com`, password `Clave1!`, phone `612345678` and card `1234567890123456` is stored
- **THEN** the file contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`

### Requirement: Save on change
After every operation that changes the set of users succeeds, the file SHALL contain exactly the current users, one line each, in the order they were created.

#### Scenario: Users written after creation
- **WHEN** Ana and then Pedro are created with `USER CREATE`
- **THEN** the file contains Ana's line followed by Pedro's line and nothing else

### Requirement: Load at startup
When the application starts, users previously stored in the file SHALL be loaded, so that they exist in the system and count for DNI and email uniqueness. If the file does not exist, the application SHALL start with no users.

#### Scenario: Users survive a restart
- **WHEN** Ana is created in one run and the application is started again
- **THEN** `USER CREATE` with Ana's DNI is rejected as a duplicate in the second run

#### Scenario: No file yet
- **WHEN** the application starts and `users.txt` does not exist
- **THEN** it starts normally with no users

#### Scenario: Corrupt line
- **WHEN** a line of the file does not have exactly seven `;`-separated attributes
- **THEN** that line is skipped, a warning is written to standard error, and the other users are loaded

### Requirement: Persistence separated and self-contained
File reading and writing SHALL be implemented in a class separate from the user class and the main class, and SHALL NOT use databases, persistence frameworks or external persistence libraries.

#### Scenario: Responsibility placement
- **WHEN** the user class and the main class are inspected
- **THEN** neither contains file reading or writing of users
