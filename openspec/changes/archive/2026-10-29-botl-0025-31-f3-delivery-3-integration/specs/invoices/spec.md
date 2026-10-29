# Spec Delta

## MODIFIED Requirements

### Requirement: User invoice
The command `INVOICE CREATE;USER;<email>` SHALL produce, for an existing user, an invoice with the user's identifying information, each individual ticket purchased directly for that user with its price, the SpeedPasses and the Special Pass associated with the user's individual tickets (including the user's tickets inside group tickets) with their prices, and the total of the items shown. Individual tickets belonging to a group ticket composition SHALL NOT be included as individual purchases. Sections without items SHALL be omitted. An unknown user SHALL be rejected with an error message.

#### Scenario: User with direct tickets
- **WHEN** `ana@correo.com` has individual tickets `T001` (40.00) and `T002` (45.50) and `INVOICE CREATE;USER;ana@correo.com` is processed
- **THEN** the invoice shows the user's email and DNI, both tickets with their prices, and a total of 85.50

#### Scenario: Group-ticket components not counted on the user invoice
- **WHEN** `ana@correo.com` is only in the composition of group ticket `TG01`
- **THEN** her invoice shows no ticket section and a total of 0.00

#### Scenario: Unknown user
- **WHEN** `INVOICE CREATE;USER;nadie@correo.com` is processed and no such user exists
- **THEN** an error message is shown

#### Scenario: Passes on the user invoice
- **WHEN** `ana@correo.com` has ticket `T001` (40.00) with SpeedPasses `SP01:1`..`SP01:10` (8.00 each) and Special Pass `PE01` (40.00), and a SpeedPass (6.40) on her component `TG01:12345678Z` of a group ticket
- **THEN** her invoice lists `T001`, a SpeedPass section with the eleven SpeedPasses, a Special Pass section with `PE01`, and a total of 166.40, without counting `TG01:12345678Z` itself
