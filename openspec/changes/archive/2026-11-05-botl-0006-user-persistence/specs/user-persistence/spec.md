# Spec Delta

## Purpose

Defines how users are preserved between runs of the application in a plain text file, the format of each stored line, and how stored users are loaded at start-up.

## ADDED Requirements

### Requirement: User file format
Users SHALL be stored in a text file named `users.txt` in the application's working directory, one user per line, with attributes in the order `FirstName;Surnames;DNI;Email;Password;Phone;Card` separated by the `;` character.

#### Scenario: Created users written one per line
- **GIVEN** an empty `users.txt`
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` and `USER CREATE;Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654` are processed
- **THEN** `users.txt` contains exactly the lines `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` and `Pedro;Lopez Martin;87654321X;pedro@correo.com;Otra2#;623456789;9876543210987654`

#### Scenario: Rejected creation does not change the file
- **GIVEN** `users.txt` containing Ana's line
- **WHEN** a `USER CREATE` with Ana's DNI is processed
- **THEN** `users.txt` is unchanged

### Requirement: Users loaded at start-up
When the application starts it SHALL load every user stored in `users.txt`, mapping each attribute of a line to the corresponding user field. If the file does not exist the application SHALL start with no users.

#### Scenario: Users recovered after restart
- **GIVEN** the application created Ana's user and then exited
- **WHEN** the application starts again
- **THEN** Ana's user is registered with all its attributes
- **AND** a new `USER CREATE` with DNI `12345678Z` is rejected as a duplicate

#### Scenario: Attributes mapped to the right fields
- **GIVEN** `users.txt` containing `Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`
- **WHEN** the users are loaded
- **THEN** the user has first name `Ana`, surnames `Garcia Lopez`, DNI `12345678Z`, email `ana@correo.com`, password `Clave1!`, telephone `612345678` and card `1234567890123456`

#### Scenario: Missing file means no users
- **GIVEN** no `users.txt` exists
- **WHEN** the application starts
- **THEN** it starts normally with no registered users

#### Scenario: Malformed line ignored
- **GIVEN** `users.txt` containing a line that does not have exactly seven `;`-separated attributes
- **WHEN** the users are loaded
- **THEN** that line is ignored, the other users are loaded and the application starts normally

### Requirement: Separated, library-free file handling
File reading and writing SHALL be implemented in a component separate from the user and the main class, using only the standard Java library (no databases, persistence frameworks or external persistence libraries).

#### Scenario: Persistence isolated from model and entry point
- **WHEN** the user class and the main class are inspected
- **THEN** neither of them reads or writes the users file
