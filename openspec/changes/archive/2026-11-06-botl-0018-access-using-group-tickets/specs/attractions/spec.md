# Spec Delta

## ADDED Requirements

### Requirement: Leave an attraction
`ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove the user with that email from the attraction's occupants, releasing one place, and print a confirmation. Users who entered together with a group ticket SHALL leave individually with their own email. An unknown attraction, or an email of nobody currently inside, MUST be rejected with an error and change nothing.

#### Scenario: User leaves and frees a place
- **GIVEN** Ana is inside `A03` (capacity 1)
- **WHEN** `ATTRACTION EXIT;A03;ana@correo.com` is processed
- **THEN** Ana is no longer inside and another valid ticket holder can enter `A03`

#### Scenario: Group members leave individually
- **GIVEN** Ana and Pedro entered `A01` with group ticket `TG01`
- **WHEN** `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** only Pedro remains inside `A01`

#### Scenario: User not inside
- **WHEN** `ATTRACTION EXIT;A01;luis@correo.com` is processed and Luis is not inside
- **THEN** an error reporting that the user is not inside `A01` is printed

### Requirement: Attraction status
`ATTRACTION STATUS;AttractionIdentifier` SHALL print the attraction's identifier, name, maximum capacity, number of users inside, number of available places and the users currently inside, all taken from the attraction's own state and independent of how users entered. It is read-only; an unknown identifier MUST produce an error.

#### Scenario: Status after mixed entries
- **GIVEN** attraction `A01` (`Dragon Rojo`, capacity 20) with Ana inside via an individual ticket and Pedro via a group ticket
- **WHEN** `ATTRACTION STATUS;A01` is processed
- **THEN** the output shows `A01`, `Dragon Rojo`, capacity `20`, `2` inside, `18` available and both emails

#### Scenario: Empty attraction status
- **GIVEN** attraction `A02` with nobody inside
- **WHEN** `ATTRACTION STATUS;A02` is processed
- **THEN** the output shows `0` inside, all places available and no users
