# Spec Delta

## MODIFIED Requirements

### Requirement: Park data files
Groups (with members), tickets, attractions (with type), and passes (type, identifier, ticket, stored price, consumed flag) SHALL be stored as text files in the active data directory, next to `users.txt`, without object serialization, databases, or persistence libraries. Every successful change SHALL be written before its confirmation is printed. If writing fails, an error SHALL be printed and the in-memory state SHALL remain as before the command.

#### Scenario: Park state survives restart
- **WHEN** users, a group with members, an individual ticket, a group ticket, and an attraction are created, and the application is restarted
- **THEN** all of them exist again with the same members, owners, dates, prices, group ticket composition, and group reference

#### Scenario: Passes and types survive restart
- **WHEN** SpeedPasses (one consumed) and a Special Pass are bought, a `SPECIAL` attraction is created, and the application restarts
- **THEN** the passes exist with the same tickets, prices, and consumed flags, and the attraction is still `SPECIAL`

#### Scenario: Untyped attraction lines load as general
- **WHEN** `attractions.txt` contains a Delivery 2 line `A01;Dragon Rojo;20`
- **THEN** `A01` is loaded as a `GENERAL` attraction
