# Spec Delta

## MODIFIED Requirements

### Requirement: User invoice items
A user invoice SHALL include the individual tickets bought directly for that user and SHALL NOT include the internal tickets generated for the user by a group ticket. It SHALL also include, in a personal passes section, the passes not refunded that are attached to any individual ticket of the user, including internal tickets of group tickets.

#### Scenario: Group ticket not counted for the member
- **WHEN** Ana has individual ticket `T001` at 40.00 and is covered by internal ticket `TG01-1` of group ticket `TG01`
- **THEN** Ana's invoice lists only `T001` and its total is `40.00`

#### Scenario: Personal passes on the user's invoice
- **WHEN** Ana has ticket `T001` (40.00) with SpeedPass `T001-S1` (8.00) and a refunded SpeedPass `T001-S2`, and internal ticket `TG01-1` with Special Pass `TG01-1-SPECIAL` (40.00)
- **THEN** her invoice lists `T001`, `T001-S1` and `TG01-1-SPECIAL` and its total is `88.00`

#### Scenario: Group invoice without passes
- **WHEN** the invoice of the group of `TG01` is generated
- **THEN** it does not list `TG01-1-SPECIAL`
