# Spec Delta

## ADDED Requirements

### Requirement: Entry with a group ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with a group ticket valid for the current system date SHALL put every user of its composition inside the attraction together; the entry is indivisible: if there is not enough capacity for all of them, or any of them is already inside, nobody enters.

#### Scenario: Whole group enters
- **WHEN** the system date is 2027-04-15, `TG01` (Ana, Pedro) is dated 2027-04-15, `A01` has 20 free places and `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** the response lists both emails and Ana and Pedro are inside `A01`

#### Scenario: Not enough room for everyone
- **WHEN** `A02` has only one free place and `ATTRACTION ENTER;A02;TG01` is processed for a two-member group ticket
- **THEN** an error about capacity is reported and neither member enters

#### Scenario: Group ticket for another date
- **WHEN** the group ticket is not dated on the current system date
- **THEN** an error is reported and nobody enters

### Requirement: Leaving an attraction
`ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove that user from the attraction's occupants and release its place; users who entered with a group ticket SHALL each leave with their own email.

#### Scenario: Member of a group leaves alone
- **WHEN** Ana and Pedro entered `A01` with `TG01` and `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** the response is `EXITED A01: ana@correo.com`, Ana is no longer inside, Pedro still is, and one more place is available

#### Scenario: User not inside
- **WHEN** `ATTRACTION EXIT;A01;luis@correo.com` is processed and Luis is not inside `A01`
- **THEN** an error is reported

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION EXIT;A99;ana@correo.com` is processed
- **THEN** an error is reported
