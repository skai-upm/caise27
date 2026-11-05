## MODIFIED Requirements

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL delete, as one consistent operation without partial deletions, all SpeedPasses and Special Passes associated with the individual tickets of its composition, the individual tickets of the composition, and the group ticket itself, and print a response starting with `OK` only when its date is strictly later than the current system date. Group tickets for the current day or earlier MUST NOT be deleted and are retained as historical records. An unknown identifier, or one that does not identify a group ticket, MUST be rejected with an `ERROR` response.

#### Scenario: Future group ticket deleted with its composition
- **WHEN** `TG01` is dated after the system date and `GROUPTICKET DELETE;TG01` is processed
- **THEN** `TG01` and every `TG01:<dni>` ticket no longer exist

#### Scenario: Today's group ticket kept
- **WHEN** `TG01` is dated on the current system date
- **THEN** `GROUPTICKET DELETE;TG01` is rejected and `TG01` and its composition still exist

#### Scenario: Composition ticket not deletable on its own
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed
- **THEN** an `ERROR` response is printed and the ticket still exists

#### Scenario: Passes of the composition deleted with the group ticket
- **GIVEN** `TG01` is dated after the system date and its composition ticket `TG01:12345678Z` has SpeedPass `SP05:1` and Special Pass `PE05`
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** `SP05:1`, `PE05`, every `TG01:<dni>` ticket and `TG01` no longer exist

#### Scenario: Rejected group ticket deletion keeps the passes
- **GIVEN** `TG01` is dated on the current system date and `TG01:12345678Z` has SpeedPass `SP05:1`
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** an `ERROR` response is printed and `TG01`, its composition and `SP05:1` still exist
