## MODIFIED Requirements

### Requirement: Create ticket
The system SHALL process `TICKET CREATE;Identificador;Correo;yyyy-MM-dd`, creating a ticket at the current ticket price when the identifier is non-blank and not used by any individual or group ticket, a registered user has that email, and the date is a valid `yyyy-MM-dd` date not earlier than the system date; on success it SHALL answer exactly `OK TICKET CREATED -> <Precio>` with the price written with two decimals.

#### Scenario: Successful creation
- **GIVEN** the system date is 2026-10-01 and user `ana@correo.com` exists
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2026-10-15` is processed
- **THEN** the response is `OK TICKET CREATED -> 40.00`

#### Scenario: Ticket for today
- **GIVEN** the system date is 2026-10-15
- **WHEN** `TICKET CREATE;T002;ana@correo.com;2026-10-15` is processed
- **THEN** the ticket is created

#### Scenario: Nonexistent user
- **WHEN** `TICKET CREATE;T003;nadie@correo.com;2026-10-15` is processed
- **THEN** an `ERROR:` response is printed and no ticket is created

#### Scenario: Past date
- **GIVEN** the system date is 2026-10-01
- **WHEN** `TICKET CREATE;T003;ana@correo.com;2026-09-30` is processed
- **THEN** an `ERROR:` response is printed and no ticket is created

#### Scenario: Duplicate identifier or invalid date
- **GIVEN** ticket `T001` exists
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2026-10-20` or `TICKET CREATE;T004;ana@correo.com;2026-10-32` is processed
- **THEN** an `ERROR:` response is printed and no ticket is created

#### Scenario: Identifier used by a group ticket
- **GIVEN** group ticket `TG01` exists
- **WHEN** `TICKET CREATE;TG01;ana@correo.com;2026-10-20` is processed
- **THEN** an `ERROR:` response is printed

### Requirement: Delete ticket
The system SHALL process `TICKET DELETE;Identificador`, deleting the ticket only if it exists and its date is strictly later than the system date. A constituent ticket of a group ticket SHALL NOT be deleted on its own; it is deleted only with its group ticket.

#### Scenario: Future ticket deleted
- **GIVEN** the system date is 2026-10-01 and ticket `T001` is dated 2026-10-15
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** an `OK:` response is printed and `T001` no longer exists

#### Scenario: Ticket for today or the past cannot be deleted
- **GIVEN** ticket `T001` is dated 2026-10-15
- **WHEN** the system date is 2026-10-15 or 2026-10-16 and `TICKET DELETE;T001` is processed
- **THEN** an `ERROR:` response is printed and `T001` is kept

#### Scenario: Unknown ticket
- **WHEN** `TICKET DELETE;T999` is processed and no such ticket exists
- **THEN** an `ERROR:` response is printed

#### Scenario: Constituent ticket
- **GIVEN** `TG01-1` is a constituent of future group ticket `TG01`
- **WHEN** `TICKET DELETE;TG01-1` is processed
- **THEN** an `ERROR:` response is printed and `TG01-1` is kept
