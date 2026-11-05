## ADDED Requirements

### Requirement: Entry restricted by attraction type
`ATTRACTION ENTER` SHALL accept, besides the previous conditions, only the ticket types allowed by the attraction type: GENERAL accepts individual and group tickets; INDIVIDUAL and SPECIAL accept individual tickets only; GROUP accepts group tickets only. A ticket of a type not accepted MUST be rejected with an error message starting with `ERROR` and no user enters.

#### Scenario: Group ticket at an individual or special attraction
- **GIVEN** valid group ticket `TG01` for today and attractions `A02` (INDIVIDUAL) and `A04` (SPECIAL)
- **WHEN** `ATTRACTION ENTER;A02;TG01` or `ATTRACTION ENTER;A04;TG01` is processed
- **THEN** an error message starting with `ERROR` is printed and nobody enters

#### Scenario: Individual ticket at a group attraction
- **GIVEN** valid individual ticket `T001` for today and attraction `A03` (GROUP)
- **WHEN** `ATTRACTION ENTER;A03;T001` is processed
- **THEN** an error message starting with `ERROR` is printed

#### Scenario: Accepted combinations
- **WHEN** `T001` enters `A01` (GENERAL) or `A02` (INDIVIDUAL), and `TG01` enters `A01` (GENERAL) or `A03` (GROUP)
- **THEN** each entry prints `ENTRY GRANTED`
