# users Specification

## Purpose
Defines the user of the Amusement Park application as an independent, encapsulated domain object with an immutable DNI, and the separate component that stores and retrieves users.

## Requirements

### Requirement: User attributes
A user SHALL have a first name, surname(s), DNI, email address, password, telephone number and card number. These attributes SHALL be accessible only through the user's operations, not as public fields.

#### Scenario: Create a user
- **WHEN** a user is created with first name `Ana`, surnames `Garcia Lopez`, DNI `12345678Z`, email `ana@correo.com`, a password, phone `612345678` and card `1234567890123456`
- **THEN** each value can be read back from the user

### Requirement: DNI is immutable
A user's DNI SHALL be fixed at creation and SHALL NOT be changeable afterwards. The other attributes MAY be changed through the user's operations.

#### Scenario: Attempt to change the DNI
- **GIVEN** an existing user with DNI `12345678Z`
- **WHEN** the user's other attributes are changed
- **THEN** the DNI is still `12345678Z`, and the user offers no operation that changes it

### Requirement: Passwords are not kept in plain text by the user
The password attribute of a user SHALL hold the stored, protected value of the password, not the original text.

#### Scenario: User created with a protected password
- **GIVEN** the password `Clave1!` protected with BCrypt
- **WHEN** a user is created with that protected value
- **THEN** the user's password attribute does not contain `Clave1!`

### Requirement: Separate store for users
A component separate from the user class and from the application's main class SHALL store and retrieve users by email and by DNI. The user class SHALL NOT manage persistence. Central park management SHALL determine user existence and DNI through this component.

#### Scenario: Retrieve stored users
- **GIVEN** user `ana@correo.com` with DNI `12345678Z` is stored
- **WHEN** users are looked up by email `ana@correo.com` and by DNI `12345678Z`
- **THEN** the same user is returned both times

#### Scenario: Park management sees stored users
- **GIVEN** user `ana@correo.com` is stored and the application is initialized
- **WHEN** `TICKET CREATE;T001;ana@correo.com;<today>` is processed
- **THEN** the ticket is created for that user
