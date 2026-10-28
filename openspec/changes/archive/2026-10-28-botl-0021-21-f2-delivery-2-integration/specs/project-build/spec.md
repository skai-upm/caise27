# Spec Delta

## ADDED Requirements

### Requirement: Delivery 2 technical restrictions
Delivery 2 SHALL keep all Delivery 1 restrictions (Java 21, Maven, executable JAR usable interactively and with a command file, standard library only, no databases, frameworks or external libraries), use a common ticket abstraction with shared price behavior and composition for group tickets, a single central park controller instance, keep tickets for the current day or earlier undeletable, and implement `--fly` with the same persistence classes configured with other paths.

#### Scenario: Delivery 1 behavior preserved
- **WHEN** the Delivery 1 example session is run on the Delivery 2 application
- **THEN** it produces the same responses as in Delivery 1

### Requirement: Delivery 2 commit roadmap
In addition to the Delivery 1 commits, the Git history SHALL contain, in this order, commits with exactly the messages `[F2-C01] Add park controller and system date`, `[F2-C02] Add user groups`, `[F2-C03] Add individual tickets`, `[F2-C04] Add group ticket purchase`, `[F2-C05] Add attractions`, `[F2-C06] Add individual attraction access`, `[F2-C07] Add group attraction access`, `[F2-C08] Extend park persistence`, `[F2-C09] Add PlantUML class model` and `[F2-C10] Complete delivery 2 integration`.

#### Scenario: Roadmap commits present in order
- **WHEN** `git log --reverse --format=%s` is inspected
- **THEN** the Delivery 1 and Delivery 2 messages appear in the listed order
