# Spec Delta

## MODIFIED Requirements

### Requirement: One individual ticket per member
A group ticket SHALL be composed of exactly one individual ticket for each user belonging to the group at purchase time, each referring to that registered user, dated with the group ticket's date and priced at the member's discounted share: the individual price used for the purchase reduced by the group's discount percentage, rounded to two decimals. Later membership changes SHALL NOT alter the composition.

#### Scenario: Composition matches members at purchase
- **GIVEN** group `G01` with Ana and Pedro
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2026-10-15` is processed
- **THEN** `TG01` contains two individual tickets, one for Ana and one for Pedro

#### Scenario: Composition frozen
- **GIVEN** `TG01` bought for `G01` with Ana and Pedro, now dated in the past
- **WHEN** Luis is added to `G01`
- **THEN** `TG01` still contains only the tickets of Ana and Pedro

#### Scenario: Discounted share stored in constituent tickets
- **GIVEN** an individual price of 40.00
- **WHEN** a group of 3 members buys `TG01`
- **THEN** each constituent ticket stores 36.00 and the group ticket total is 108.00
