# users Specification

## Purpose

Defines the park's users as independent domain objects: the data each user keeps, its encapsulation, and the immutability of the DNI.

## Requirements

### Requirement: User data
Each user SHALL be an independent object with first name, surname(s), DNI, email address, password, telephone number and card number. The password SHALL be kept only in its protected form.

#### Scenario: User keeps all attributes
- **WHEN** a user is created with first name `Ana`, surnames `Garcia Lopez`, DNI `12345678Z`, email `ana@correo.com`, password `Clave1!`, phone `612345678` and card `1234567890123456`
- **THEN** each value can be read back from the user, and the password is available only as a protected value that verifies `Clave1!`

### Requirement: Encapsulated attributes
User attributes SHALL be private and accessible only through the user's operations; the DNI SHALL NOT be changeable after creation.

#### Scenario: Fields are private
- **WHEN** the user class is inspected
- **THEN** every attribute is private and the DNI attribute is final with no operation that changes it

#### Scenario: Other attributes changeable through operations
- **WHEN** the telephone of a user is changed through its operation
- **THEN** the new value is returned and the DNI is unchanged

### Requirement: User data persisted
Every user attribute SHALL be stored and reconstructed by the users repository in a text file with one user per line and the attributes separated by `;` in the order `FirstName;Surnames;DNI;Email;Password;Phone;Card`, the password only in protected form; previously stored users SHALL be loaded when the application starts, and stored users with fewer fields SHALL still be read.

#### Scenario: Restart keeps all user data
- **WHEN** a complete user is saved and the application restarts
- **THEN** all seven attributes are reconstructed and the original password still verifies

#### Scenario: Assignment line format
- **WHEN** user Ana (`Ana`, `Garcia Lopez`, `12345678Z`, `ana@correo.com`, password `Clave1!`, `612345678`, `1234567890123456`) is saved
- **THEN** the users file contains the line `Ana;Garcia Lopez;12345678Z;ana@correo.com;<protected password>;612345678;1234567890123456`, where the password field is a BCrypt value and not `Clave1!`

#### Scenario: Users created by command reloaded at start
- **WHEN** an execution creates Ana and Pedro with USER CREATE and a new execution starts in normal mode
- **THEN** both users exist with their data

### Requirement: Separate user, application and storage classes
The user SHALL be represented by its own class that does not manage persistence; the class that starts and runs the application SHALL NOT contain file reading or writing logic; users SHALL be stored and retrieved by a separate class.

#### Scenario: Responsibilities separated
- **WHEN** the sources are inspected
- **THEN** the user class performs no I/O, the entry class does not read or write files, and a repository class stores and retrieves users

### Requirement: DNI validation
A DNI SHALL be valid only if it has eight digits followed by an upper-case letter that matches the number according to the standard Spanish algorithm (the letter at position `number mod 23` of `TRWAGMYFPDXBNJZSQVHLCKE`).

#### Scenario: Correct and wrong letter
- **WHEN** `12345678Z` and `12345678A` are checked
- **THEN** the first is valid and the second is invalid

#### Scenario: Malformed DNI
- **WHEN** `1234567Z`, `123456789Z`, `1234567AZ` or `12345678` is checked
- **THEN** it is invalid

### Requirement: Email validation
An email address SHALL be valid only if it has the form `text@text.xxx`: content before a single `@`, a non-empty domain name after it, and an extension of exactly three characters after the last dot, checked with basic string operations and `split`, without regular expressions.

#### Scenario: Source examples
- **WHEN** `ana@correo.com`, `ana`, `ana@correo`, `@correo.com`, `ana@.com` and `ana@correo.es` are checked
- **THEN** only `ana@correo.com` is valid

### Requirement: Password rules
A password SHALL be valid only if it contains at least one upper-case letter, one lower-case letter, one digit and one symbol, checked without regular expressions or external libraries.

#### Scenario: Source examples
- **WHEN** `Clave1!`, `clave1!`, `CLAVE1!`, `Clave!!` and `Clave12` are checked
- **THEN** only `Clave1!` is valid

### Requirement: Card number validation
A card number SHALL be valid only if it consists of exactly 16 consecutive digits, with no spaces, letters or symbols.

#### Scenario: Source examples
- **WHEN** `1234567890123456`, `1234 5678 9012 3456`, `123456789012345` and `12345678901234567` are checked
- **THEN** only `1234567890123456` is valid

### Requirement: Reusable user data check
The model SHALL offer a single check of a user's DNI, email, password and card that rejects invalid data with an error naming every invalid field, so that user operations can reuse it; the telephone number SHALL only be stored, without validation.

#### Scenario: Several invalid fields reported
- **WHEN** the data DNI `12345678A`, email `ana@correo`, password `Clave1!` and card `123` is checked
- **THEN** an error names DNI, email and card as invalid

### Requirement: USER CREATE command
The command `USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card` SHALL create and register a user after validating its data with the user data rules; the password SHALL be stored only in protected form. Invalid data or a wrong number of fields SHALL be rejected with an error message, nothing SHALL be created, and processing SHALL continue.

#### Scenario: Source example
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456` is processed
- **THEN** `USER CREATED` is shown and user `ana@correo.com` exists with all its data and a protected password that verifies `Clave1!`

#### Scenario: Invalid data rejected
- **WHEN** a USER CREATE line has DNI `12345678A` or password `clave1!` or card `1234 5678 9012 3456` or email `ana@correo.es`
- **THEN** an error message is shown and no user is created

#### Scenario: Wrong number of fields
- **WHEN** `USER CREATE;Ana;Garcia Lopez;12345678Z` is processed
- **THEN** an error message is shown and processing continues

### Requirement: Unique DNI and email
Before creating a user, the system SHALL check that no registered user has the same DNI and that no registered user has the same email address; otherwise the creation SHALL be rejected with an error message.

#### Scenario: Duplicate DNI
- **WHEN** `12345678Z` is registered and another USER CREATE uses DNI `12345678Z` with a different email
- **THEN** an error message is shown and no user is created

#### Scenario: Duplicate email
- **WHEN** `ana@correo.com` is registered and another USER CREATE uses that email with DNI `87654321X`
- **THEN** an error message is shown and no user is created

### Requirement: USER DELETE command
The command `USER DELETE;Email;Password` SHALL locate the user by email, verify that it exists, check the password against the stored protected value, and only then delete the user and update persistent data. An unknown user, a wrong password, a wrong number of fields, or a user that the ticket rules forbid deleting SHALL be rejected with an error message and nothing SHALL change.

#### Scenario: Source example
- **WHEN** user `ana@correo.com` with password `Clave1!` exists without active tickets and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** `USER DELETED` is shown, the user no longer exists, and after a restart it is still absent

#### Scenario: Wrong password
- **WHEN** `USER DELETE;ana@correo.com;clave1!` is processed
- **THEN** an error message is shown and the user still exists

#### Scenario: Unknown user
- **WHEN** `USER DELETE;nadie@correo.com;Clave1!` is processed
- **THEN** an error message is shown

#### Scenario: Active ticket blocks deletion
- **WHEN** the user has a ticket for the current date and the correct password is given
- **THEN** an error message is shown and the user still exists

### Requirement: Deleted user leaves its groups
When a user is deleted, the system SHALL remove the user from every group it belongs to, while historical tickets SHALL keep the information needed to reconstruct them.

#### Scenario: Group membership removed, history kept
- **WHEN** `pedro@correo.com` belongs to group `G01`, had a group ticket that is now historical, and is deleted
- **THEN** `G01` no longer contains him, and the historical group ticket still lists his individual ticket with his email and DNI after a restart
