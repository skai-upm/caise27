# persistence Specification

## Purpose

Defines which park state the application preserves in text files between executions, how it is reconstructed at start-up, and the temporary `--fly` execution mode that isolates an execution's data.

## Requirements

### Requirement: Park state persisted in text files
The application SHALL store the park state in text files and reconstruct it when it starts again: users needed by other elements, groups and their users, individual tickets and their user, group tickets with their individual tickets and, while the group exists, the reference to the purchasing group, and attractions. Automatic Java object serialization, databases, persistence frameworks and external libraries SHALL NOT be used.

#### Scenario: State survives a restart
- **WHEN** an execution creates group `G01` with two users, an individual ticket, a group ticket for `G01` and attraction `A01`, and the application is started again with the same data directory
- **THEN** the new execution has `G01` with the same users, the individual ticket with its user, date and price, the group ticket with the same composition, prices and group reference, and `A01` with its name and capacity

#### Scenario: Historical group ticket of a deleted group
- **WHEN** a group with only historical group tickets was deleted and the application restarts
- **THEN** the group ticket is reconstructed with its identifier, date, price and composition and a null group reference

### Requirement: Attraction occupants not persisted
The users inside attractions SHALL NOT be persisted; at the start of every execution all attractions SHALL be empty.

#### Scenario: Attractions empty after restart
- **WHEN** users were inside `A01` when an execution ended and the application starts again
- **THEN** `A01` exists and has no users inside

### Requirement: Persistence separated from the domain
Persistence SHALL be handled by repository components separate from the domain objects and from the command interface; domain objects SHALL NOT store or retrieve their own persistent representation, and repositories SHALL be configured only by the location of their files.

#### Scenario: Same repositories, different directory
- **WHEN** the repositories are configured with two different directories
- **THEN** each configuration reads and writes only the files inside its own directory using the same repository implementation

### Requirement: Temporary --fly execution mode
When `--fly` is the first argument, the application SHALL create an independent temporary directory named by a timestamp under `tmp/` (for example `tmp/20260826_190315/`), all repositories SHALL use only files inside it, and the directory and its files SHALL be deleted when the execution ends, both through EXIT and when processing of the command file finishes normally. `--fly` MAY be followed by a command file path; without `--fly` the arguments SHALL behave as before.

#### Scenario: Supported execution forms
- **WHEN** the application is started as `java -jar <jar>`, `java -jar <jar> input.txt`, `java -jar <jar> --fly` or `java -jar <jar> --fly input.txt`
- **THEN** it runs interactively or with the file accordingly, using permanent persistence only in the first two forms

#### Scenario: Fly mode leaves no data
- **WHEN** `java -jar <jar> --fly input.txt` creates groups, tickets and attractions and the file ends
- **THEN** the temporary directory no longer exists and the permanent data files are unchanged

#### Scenario: Fly mode starts empty
- **WHEN** permanent data exists and the application is started with `--fly`
- **THEN** the execution sees none of the permanent data
