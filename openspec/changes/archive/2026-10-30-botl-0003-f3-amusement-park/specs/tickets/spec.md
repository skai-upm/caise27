## MODIFIED Requirements

### Requirement: Delete individual ticket
`TICKET DELETE;<identifier>` SHALL delete a stand-alone individual ticket whose date is strictly after the current system date, together with all its SpeedPasses and its Special Pass. Tickets dated today or earlier, individual tickets belonging to a group ticket, group-ticket identifiers and unknown identifiers SHALL be rejected.

#### Scenario: Delete future ticket
- **WHEN** T001 is dated 2027-04-16, the system date is 2027-04-15 and `TICKET DELETE;T001` is processed
- **THEN** T001 no longer exists

#### Scenario: Ticket for today kept
- **WHEN** T001 is dated today and `TICKET DELETE;T001` is processed
- **THEN** an error is printed and T001 still exists

#### Scenario: Composition ticket cannot be deleted alone
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed for a ticket inside group ticket TG01
- **THEN** an error is printed and TG01 is unchanged

#### Scenario: Passes deleted with ticket
- **WHEN** future ticket T001 has SpeedPasses and a Special Pass and `TICKET DELETE;T001` is processed
- **THEN** none of those passes exists afterwards

### Requirement: Delete group ticket
`GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket dated strictly after the current system date together with the passes of its composition tickets and the composition itself. Group tickets dated today or earlier SHALL be kept and the command rejected.

#### Scenario: Delete future group ticket
- **WHEN** TG01 is dated after today and `GROUPTICKET DELETE;TG01` is processed
- **THEN** TG01 and all `TG01:<dni>` tickets no longer exist

#### Scenario: Historical group ticket kept
- **WHEN** TG01 is dated today or earlier and `GROUPTICKET DELETE;TG01` is processed
- **THEN** an error is printed and TG01 still exists

#### Scenario: Member passes deleted
- **WHEN** a composition ticket of future TG01 has passes and `GROUPTICKET DELETE;TG01` is processed
- **THEN** those passes no longer exist
