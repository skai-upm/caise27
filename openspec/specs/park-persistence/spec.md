# park-persistence Specification

## Purpose
Defines how the park state beyond users — groups, tickets and attractions with their relationships — is preserved between runs in text files, and the temporary `--fly` mode that isolates an execution from the permanent data.

## Requirements

### Requirement: Groups are persisted
Groups SHALL be stored with their identifier, name and members and SHALL be reconstructed with the same members when the application starts again.

#### Scenario: Group survives a restart
- **WHEN** group `G01` with Ana and Pedro is created in one run and the application is started again on the same data
- **THEN** `G01` exists with Ana and Pedro as members and `GROUP ADD;G01;ana@correo.com` is rejected as a duplicate member

### Requirement: Tickets are persisted
Individual tickets SHALL be stored with their identifier, date, price and user; group tickets with their identifier, date, base price, composition and — while the group exists — the purchasing group. On restart, tickets SHALL be reconstructed with the same identifiers, dates, prices and relationships, so ticket rules (shared identifiers, locks, access) apply to them.

#### Scenario: Group ticket survives a restart
- **WHEN** `TG01` is bought by `G01` (two members, 40.00 each) in one run and the application is started again
- **THEN** `TG01` exists with total 80.00, its composition tickets `TG01:<dni>` reference the members, and `G01` is still locked while `TG01` is active

#### Scenario: Prices are kept
- **WHEN** a ticket was bought at 45.50 in a previous run
- **THEN** after restart its price is still 45.50, although the base price is back to 40.00

### Requirement: Historical records are reconstructible
Tickets of users or groups that no longer exist SHALL be reconstructed from the stored data: the ticket keeps the user's DNI, name, surnames and email, and a group ticket of a deleted group is reconstructed without a group reference but with its identifier, date, price and composition.

#### Scenario: Ticket of a deleted user
- **WHEN** Ana's historical ticket `T001` exists, Ana was deleted, and the application is started again
- **THEN** `T001` is reconstructed with Ana's DNI and email

### Requirement: Attractions are persisted empty
Attractions SHALL be stored with identifier, name and capacity. Their occupants SHALL NOT be stored: at the start of every execution every attraction SHALL be empty.

#### Scenario: Attraction after restart
- **WHEN** Ana was inside `A01` when the previous execution ended
- **THEN** after restart `A01` exists with the same name and capacity and `ATTRACTION LIST;A01` reports no users

### Requirement: Persistence technology and responsibilities
The park data SHALL be stored in text files, written and read by persistence classes separate from the domain and main classes, without Java object serialization, databases, persistence frameworks or external libraries.

#### Scenario: Plain text files
- **WHEN** the data directory is inspected after a run
- **THEN** it contains readable text files for users, groups, tickets and attractions

### Requirement: Temporary persistence mode
When `--fly` is the first argument, the application SHALL create a new directory `tmp/<yyyyMMdd_HHmmss>/` (timestamp of startup), configure every persistence class (users, groups, tickets, attractions) to use files only inside it, and delete that directory with all its files when the execution ends, whether by `EXIT`, end of the command file or end of standard input. The persistence classes SHALL be the same as in normal mode, only configured with other paths.

#### Scenario: Fly mode leaves permanent data untouched
- **WHEN** `java -jar target/amusement-park.jar --fly input.txt` runs a file that creates users and groups
- **THEN** the commands behave as in normal mode, the permanent data files are unchanged, and no `tmp/<timestamp>/` directory remains afterwards

#### Scenario: Fly mode starts empty
- **WHEN** permanent data contains Ana and the application runs with `--fly`
- **THEN** Ana does not exist during that execution
