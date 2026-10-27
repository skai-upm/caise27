# Spec Delta

## ADDED Requirements

### Requirement: Entering with an individual ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` SHALL let the user of an existing individual ticket into the existing attraction when the ticket's date equals the current system date and the attraction has at least one available place. The date SHALL be checked only at entry; a user already inside SHALL stay inside when the system date changes.

#### Scenario: Valid entry
- **WHEN** the system date is `2027-04-15`, Ana has ticket `T001` dated `2027-04-15`, `A01` has capacity 20 and is empty, and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** a confirmation is printed and Ana is inside `A01` with 19 places available

#### Scenario: Ticket for another date
- **WHEN** the system date is `2027-04-14` and `ATTRACTION ENTER;A01;T001` is processed for ticket `T001` dated `2027-04-15`
- **THEN** an error is printed and nobody enters

#### Scenario: Unknown ticket or attraction
- **WHEN** `ATTRACTION ENTER;A01;T999` (no such ticket) or `ATTRACTION ENTER;A99;T001` (no such attraction) is processed
- **THEN** an error is printed and nobody enters

#### Scenario: Attraction full
- **WHEN** attraction `A03` has capacity 1 with Pedro inside and `ATTRACTION ENTER;A03;T001` is processed with Ana's valid ticket
- **THEN** an error about insufficient capacity is printed and Ana does not enter

#### Scenario: Presence independent of later date changes
- **WHEN** Ana entered `A01` on `2027-04-15` and then `SYSTEM DATE;2027-04-16` is processed
- **THEN** Ana is still inside `A01`

#### Scenario: Already inside
- **WHEN** Ana is inside `A01` and `ATTRACTION ENTER;A01;T001` is processed again
- **THEN** an error is printed and Ana appears only once among `A01`'s users
