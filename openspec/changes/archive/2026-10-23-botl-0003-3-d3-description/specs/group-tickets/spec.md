# Spec Delta

## MODIFIED Requirements

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket only when its date is strictly after the current system date. It SHALL delete every pass of the composition's individual tickets, then those tickets, then the group ticket. It SHALL persist atomically, leaving no partial deletion, and print `GROUPTICKET DELETED`. Group tickets dated today or earlier, unknown identifiers, and other ticket kinds SHALL be rejected with an error.

#### Scenario: Delete future group ticket
- **WHEN** the system date is `2027-04-15` and `GROUPTICKET DELETE;TG02` is processed for a group ticket dated `2027-04-20`
- **THEN** `GROUPTICKET DELETED` is printed and neither `TG02` nor any `TG02:<dni>` ticket exists

#### Scenario: Today's group ticket kept
- **WHEN** `GROUPTICKET DELETE;TG01` is processed for a group ticket dated on the current system date
- **THEN** an error is printed and `TG01` still exists

#### Scenario: Member passes deleted with the group ticket
- **WHEN** members' tickets of future group ticket `TG02` have SpeedPasses and a Special Pass, and `GROUPTICKET DELETE;TG02` is processed
- **THEN** none of those passes exists afterwards
