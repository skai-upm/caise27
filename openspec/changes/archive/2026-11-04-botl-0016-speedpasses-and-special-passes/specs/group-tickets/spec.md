# Spec Delta

## MODIFIED Requirements

### Requirement: Group ticket composition
A group ticket SHALL have an identifier, a date, a reference to the group that bought it, and SHALL be composed of one individual ticket per member of the group at the moment of purchase. Each constituent individual ticket SHALL have the group ticket's date, refer to its member user, and store the discounted per-member price: the individual price in force at purchase reduced by the group's discount percentage (two decimals, half up). Later changes to the group's membership SHALL NOT change an existing group ticket's composition.

#### Scenario: One ticket per member
- **GIVEN** group `G01` has members Ana, Pedro and Luis
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2026-10-15` succeeds
- **THEN** `TG01` contains exactly three individual tickets dated 2026-10-15, one for each of Ana, Pedro and Luis

#### Scenario: Composition is a snapshot
- **GIVEN** `TG01` was bought for `G01` with three members and its date has passed
- **WHEN** a member is removed from `G01`
- **THEN** `TG01` still contains three individual tickets

#### Scenario: Stored per-member price
- **GIVEN** the individual price is 40.00 and `G01` has 3 members
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2026-10-15` succeeds
- **THEN** each constituent ticket stores 36.00, while the group ticket's price is 108.00
