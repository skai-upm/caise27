# attraction-access Specification

## Purpose
Defines how visitors enter and leave attractions using individual or group tickets valid for the system date, without ever exceeding capacity, and how the live status of an attraction is reported.

## Requirements

### Requirement: Uniform ticket-based entry
The system SHALL process `ATTRACTION ENTER;Identificador;Ticket`, accepting the identifier of an individual ticket (admitting its owner) or of a group ticket (admitting the owners of all its constituent tickets), through the same entry rules for both kinds.

#### Scenario: Individual ticket entry
- **GIVEN** system date 2026-10-15, attraction `A01` with free places and ticket `T001` of Ana dated 2026-10-15
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed
- **THEN** an `OK:` response is printed and Ana is present in `A01`

#### Scenario: Group ticket entry
- **GIVEN** group ticket `TG01` dated today with constituents for Pedro, Luis and Marta, and enough places
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** Pedro, Luis and Marta are present in `A01`

#### Scenario: Unknown attraction or ticket
- **WHEN** the attraction or the ticket does not exist
- **THEN** an `ERROR:` response is printed and nobody enters

### Requirement: Ticket valid for the system date
Entry SHALL be accepted only when the ticket's date equals the current system date.

#### Scenario: Ticket for another day
- **GIVEN** ticket `T001` dated 2026-10-15
- **WHEN** the system date is 2026-10-14 or 2026-10-16 and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** an `ERROR:` response is printed and nobody enters

### Requirement: Capacity never exceeded and atomic group entry
Entry SHALL be rejected as a whole, admitting nobody, when the number of visitors of the ticket exceeds the attraction's available places, or when any of them is already present in that attraction.

#### Scenario: Group larger than the remaining places
- **GIVEN** `A01` has 3 available places
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed for a group ticket of 4 users
- **THEN** an `ERROR:` response is printed and none of the 4 users enters

#### Scenario: Exactly full
- **GIVEN** `A01` has 3 available places
- **WHEN** a group ticket of 3 users enters
- **THEN** all 3 enter and `A01` has 0 available places

#### Scenario: Already inside
- **GIVEN** Ana is present in `A01`
- **WHEN** a ticket whose visitors include Ana is used to enter `A01`
- **THEN** an `ERROR:` response is printed and nobody enters

### Requirement: Leaving an attraction
The system SHALL process `ATTRACTION EXIT;Identificador;Ticket`, removing from the attraction the ticket's visitors who are present and freeing their places, without checking the ticket's date; it SHALL be rejected if none of them is present.

#### Scenario: Exit frees capacity
- **GIVEN** Ana entered `A01` with `T001`
- **WHEN** `ATTRACTION EXIT;A01;T001` is processed
- **THEN** an `OK:` response is printed, Ana is no longer present and one more place is available

#### Scenario: Exit on a later date
- **GIVEN** Ana entered with `T001` and then `SYSTEM DATE` moved to the next day
- **WHEN** `ATTRACTION EXIT;A01;T001` is processed
- **THEN** Ana leaves

#### Scenario: Nobody to remove
- **WHEN** `ATTRACTION EXIT;A01;T001` is processed and Ana is not in `A01`
- **THEN** an `ERROR:` response is printed

### Requirement: Attraction status
The system SHALL process `ATTRACTION STATUS;Identificador`, answering with an `OK:` response that shows the identifier, name, type, maximum capacity, number of users present, number of available places and the users present, reflecting the state at that moment and counting every present user once regardless of the ticket kind used.

#### Scenario: Status after mixed entries and an exit
- **GIVEN** `A01` (capacity 20); Ana entered with `T001` and Pedro, Luis and Marta with `TG01`
- **WHEN** `ATTRACTION STATUS;A01` is processed
- **THEN** it shows `A01`, `Tornado`, capacity 20, 4 present, 16 available and lists Ana, Pedro, Luis and Marta
- **WHEN** `ATTRACTION EXIT;A01;T001` and then `ATTRACTION STATUS;A01` are processed
- **THEN** it shows 3 present, 17 available and no longer lists Ana

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION STATUS;A99` is processed and `A99` does not exist
- **THEN** an `ERROR:` response is printed

#### Scenario: Status shows the type
- **GIVEN** GROUP attraction `A02`
- **WHEN** `ATTRACTION STATUS;A02` is processed
- **THEN** the response shows the type GROUP
