# Spec Delta

## MODIFIED Requirements

### Requirement: Time-dependent logic uses the system date
Every time-dependent business rule SHALL obtain the current date from the application's system date and SHALL NOT read the computer's real date directly. The operating system clock SHALL only be read to initialise the system date at startup and to build technical identifiers that are not business rules, such as the name of the `--fly` temporary directory.

#### Scenario: Code review of date access
- **WHEN** the production sources are reviewed
- **THEN** the operating system's date is read only when initialising the system date at startup and when naming the `--fly` temporary directory
