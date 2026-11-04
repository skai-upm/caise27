## MODIFIED Requirements

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
