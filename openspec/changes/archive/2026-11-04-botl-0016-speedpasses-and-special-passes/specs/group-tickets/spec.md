## MODIFIED Requirements

### Requirement: Group ticket composition
A group ticket SHALL reference (aggregate) the group that bought it and SHALL be composed of exactly one individual ticket per member of the group at the moment of purchase; each constituent ticket SHALL reference its user, have the group ticket's date and, as its stored price, its discounted share (the individual price used for the purchase reduced by the group discount, rounded to two decimals), and be identified `<group ticket id>-<n>`.

#### Scenario: One ticket per member
- **GIVEN** group `G01` with members Ana, Pedro and Luis, system date 2026-10-01 and price 40.00
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2026-10-15` is processed
- **THEN** individual tickets `TG01-1`, `TG01-2`, `TG01-3` dated 2026-10-15 at 36.00 (40.00 less the 10% discount) exist for Ana, Pedro and Luis respectively, and `TG01` is composed of them

#### Scenario: Composition frozen at purchase
- **GIVEN** `TG01` was bought for `G01` with three members and its date has passed
- **WHEN** a member is later added to or removed from `G01`
- **THEN** `TG01` still has the same three constituent tickets

#### Scenario: Share without discount
- **WHEN** a group of 2 buys at 40.00
- **THEN** each constituent ticket is stored at 40.00
