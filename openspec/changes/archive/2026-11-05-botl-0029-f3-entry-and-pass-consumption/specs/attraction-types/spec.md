## MODIFIED Requirements

### Requirement: Special attractions require an unconsumed Special Pass
Entry to a `SPECIAL` attraction with an individual ticket SHALL additionally require that the ticket has a Special Pass that has not been consumed; the user SHALL NOT specify any additional option for this. When the ticket has no Special Pass, or its Special Pass is consumed, the entry SHALL be rejected with an `ERROR` response and nobody enters. The Special Pass SHALL be marked as consumed only when the entry completes successfully, as defined by the pass consumption requirements.

#### Scenario: Ticket with an unconsumed Special Pass enters
- **GIVEN** `SPECIAL` attraction `A04` and ticket `T001`, valid today, with unconsumed Special Pass `PE01`
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed
- **THEN** a response starting with `OK` is printed, the ticket's user is inside `A04`, and the response states that Special Pass `PE01` was consumed

#### Scenario: Ticket without a Special Pass rejected
- **WHEN** `ATTRACTION ENTER;A04;T002` is processed and `T002` has no Special Pass
- **THEN** an `ERROR` response mentions the missing Special Pass and nobody enters

#### Scenario: Consumed Special Pass rejected
- **GIVEN** the Special Pass of `T001` has been consumed
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed
- **THEN** an `ERROR` response is printed and nobody enters
