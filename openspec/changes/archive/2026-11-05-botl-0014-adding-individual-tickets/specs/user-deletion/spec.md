## ADDED Requirements

### Requirement: Active tickets block user deletion
A user SHALL NOT be deleted while any active ticket (date equal to or later than the current system date) is associated with them; the deletion is rejected with an error message starting with `ERROR` and nothing changes. Historical tickets (earlier dates) SHALL NOT prevent deletion and SHALL be retained after the user is deleted. The blocked state is derived from the existing tickets and the current date, not stored in the user.

#### Scenario: Active ticket blocks deletion
- **GIVEN** current date 2027-04-01 and Ana has ticket `T001` on 2027-04-15
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error message starting with `ERROR` is printed and Ana is still registered

#### Scenario: Ticket for today blocks deletion
- **GIVEN** current date 2027-04-15 and Ana has ticket `T001` on 2027-04-15
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the deletion is rejected

#### Scenario: Only historical tickets
- **GIVEN** Ana's only ticket `T001` is dated 2027-04-15 and the current date is 2027-04-16
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** `USER DELETED` is printed and ticket `T001` is still retained

#### Scenario: Deleting the future ticket unblocks the user
- **GIVEN** current date 2027-04-01 and Ana's only ticket `T001` on 2027-04-15
- **WHEN** `TICKET DELETE;T001` and then `USER DELETE;ana@correo.com;Clave1!` are processed
- **THEN** both succeed
