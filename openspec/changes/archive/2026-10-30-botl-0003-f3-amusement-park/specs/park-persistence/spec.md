# Spec Delta

## MODIFIED Requirements

### Requirement: Temporary execution mode
When the first argument is `--fly`, optionally followed by a command file path, the application SHALL create a new directory `tmp/<yyyyMMdd_HHmmss>/`, configure all repositories (users, groups, tickets, attractions, passes) to use only files there, and delete that directory and its files when execution ends via EXIT, the end of the command file or the end of input. The same repository implementations SHALL be used, differing only in paths. Without `--fly`, argument handling SHALL be unchanged.

#### Scenario: Fly with command file leaves no data
- **WHEN** `java -jar target/nombre-aplicacion.jar --fly input_fase2.txt` creates users, groups, tickets and attractions
- **THEN** the commands run normally, the persistent files of normal mode are untouched, and no temporary directory remains after execution

#### Scenario: Fly interactive
- **WHEN** the JAR is started with `--fly` only and receives commands on standard input followed by `EXIT`
- **THEN** commands run normally and the temporary directory is deleted on exit

#### Scenario: Normal mode unchanged
- **WHEN** the JAR is started without arguments or with only a command file path
- **THEN** behaviour is as in Delivery 1, with data in the working directory

#### Scenario: Fly covers passes
- **WHEN** `java -jar target/nombre-aplicacion.jar --fly input_fase3.txt` buys SpeedPasses and Special Passes
- **THEN** no pass file of normal mode is created or modified and no temporary data remains

## ADDED Requirements

### Requirement: Persist attraction types and passes
The type of each attraction and every SpeedPass and Special Pass (identifier, kind, ticket, stored price, consumed state) SHALL be persisted and reconstructed. Attraction lines without a type SHALL load as GENERAL. Consumed passes SHALL stay persisted while their ticket exists.

#### Scenario: Types and passes reconstructed
- **WHEN** a SPECIAL attraction, a consumed SpeedPass and an unconsumed Special Pass exist and the application is restarted
- **THEN** the attraction is SPECIAL and both passes exist with their ticket, price and consumed state

#### Scenario: Delivery 2 attraction file
- **WHEN** `attractions.txt` contains the line `A01;Dragon Rojo;20`
- **THEN** `A01` loads as a GENERAL attraction
