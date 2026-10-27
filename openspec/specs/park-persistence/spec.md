# park-persistence Specification

## Purpose

Preserves the park state — groups, tickets and attractions with their relationships — between executions in text files, and offers a temporary mode whose data disappears when the execution ends.

## Requirements

### Requirement: Groups persisted with their members
Groups SHALL be stored in a text file with their identifier, name and members, and rebuilt at start-up referencing the loaded user objects.

#### Scenario: Group survives restart
- **WHEN** group `G01` with Ana and Pedro is created and the application is restarted
- **THEN** `G01` exists with the same name and both members

### Requirement: Tickets persisted with their relationships
Individual tickets SHALL be stored with identifier, date, base price and their user; group tickets with identifier, date, base price, the purchasing group while it exists, and their composition; at start-up tickets SHALL reference the loaded users and groups, keeping prices and derived component identifiers.

#### Scenario: Individual ticket survives restart
- **WHEN** ticket `T001` for Ana at 45.50 is created and the application restarts
- **THEN** `T001` exists for Ana with price 45.50 and still blocks deleting Ana while active

#### Scenario: Group ticket survives restart
- **WHEN** group ticket `TG01` for `G01` (3 members, base 40.00) is created and the application restarts
- **THEN** `TG01` references `G01`, has components `TG01:<dni>` for the three members and total 108.00, and `G01` is still locked while it is active

### Requirement: Historical records preserved
A historical ticket of a deleted user, and a historical group ticket of a deleted group, SHALL be rebuilt at start-up with identifier, date, price and composition; the group reference of the latter SHALL be empty.

#### Scenario: Ticket of a deleted user
- **WHEN** Ana's ticket became historical, Ana was deleted and the application restarts
- **THEN** the ticket still exists with Ana's DNI and its price, and Ana is not a registered user

#### Scenario: Group ticket of a deleted group
- **WHEN** a group with only historical group tickets was deleted and the application restarts
- **THEN** its group tickets exist with no group reference and their original composition

### Requirement: Attractions persisted empty
Attractions SHALL be stored with identifier, name and capacity; occupants SHALL NOT be stored, and every execution SHALL start with all attractions empty.

#### Scenario: Attraction survives restart empty
- **WHEN** Ana is inside `A01` when the execution ends and the application is started again
- **THEN** `A01` exists with its name and capacity and nobody inside

### Requirement: Text files without serialization
Persistence SHALL use text files written and read by dedicated persistence classes, without Java object serialization, databases or external libraries.

#### Scenario: Source inspection
- **WHEN** the production sources are inspected
- **THEN** no `ObjectOutputStream`/`Serializable` is used and file access is confined to persistence classes

### Requirement: Temporary persistence mode
With `--fly` as first argument the application SHALL create a directory `tmp/<yyyyMMdd_HHmmss>/`, configure every repository to use only files inside it, and delete it with all its files when the execution ends through EXIT or the end of the input; permanent data SHALL NOT be read or modified.

#### Scenario: Fly run with a command file
- **WHEN** `java -jar target/nombre-aplicacion.jar --fly input_fase2.txt` creates users, groups, tickets and attractions
- **THEN** the commands behave as in a normal run, the permanent data files are unchanged and no temporary directory remains afterwards

#### Scenario: Fly run ended with EXIT
- **WHEN** an interactive `--fly` session ends with `EXIT`
- **THEN** the temporary directory has been deleted

#### Scenario: Fly runs are isolated
- **WHEN** the same command file is run twice with `--fly`
- **THEN** both runs produce identical output, because neither sees data from the other or from normal runs
