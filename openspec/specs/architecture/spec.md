# architecture Specification

## Purpose

Captures the structural design constraints of the assignment that graders verify by inspecting and testing the code, beyond observable command behavior.

## Requirements

### Requirement: Layered packages
The code SHALL be organized under `es.upm.amusementpark` into `presentation` (command reading, interpretation, and output), `business` (model, rules, and controllers), and `persistence` (repositories and data files). An application entry point in the root package wires them. Presentation classes SHALL NOT use persistence classes, and model classes SHALL NOT read or write their own persistent representation.

#### Scenario: Presentation does not touch persistence
- **WHEN** the presentation package's classes are inspected
- **THEN** none of them references a class of the persistence package

### Requirement: Exceptions for abnormal situations
Business operations SHALL signal rejected or failed operations with exceptions from a park exception hierarchy that distinguishes invalid data, missing entities, disallowed operations, and persistence failures. The presentation layer SHALL catch them per command and report them as `ERROR` lines, and processing SHALL continue.

#### Scenario: Exception becomes an error line
- **WHEN** a command refers to a missing ticket
- **THEN** a not-found exception is raised in the business layer and the command prints an `ERROR` line while later commands still run

### Requirement: Singleton controllers with injected dependencies
Each controller with a unique management responsibility (park, system settings, users, groups, tickets, passes, attractions, invoices) SHALL be a Singleton. Repositories SHALL be interfaces with file implementations that are not singletons. They SHALL be created and configured with the data directory at startup and injected into the controllers, together with the controllers' collaborators, without any dependency injection framework. Normal and `--fly` execution SHALL differ only in the repository configuration.

#### Scenario: Different repositories, same controllers
- **WHEN** the application starts once in normal mode and once with `--fly`
- **THEN** the same controller instances work against repositories configured with different directories

### Requirement: Builder for invoices
Invoices SHALL be built step by step through a common builder abstraction with distinct user and group builders. A director SHALL add only the sections for which data exists, without constructors with many optional parameters.

#### Scenario: Builders share an abstraction
- **WHEN** the invoice classes are inspected
- **THEN** the user and group builders implement the same builder abstraction and the invoice is obtained from the builder's build step

### Requirement: Injected help formatting
The component that generates help text SHALL receive the formatting behavior by dependency injection. `HELP` and `HELP EXTENDED` SHALL be produced by the same generator logic with different injected formatters, independent of command reading and execution.

#### Scenario: Two formatters, one generator
- **WHEN** help output is produced
- **THEN** the basic and extended outputs come from the same generator type configured with different formatter implementations

### Requirement: Generic attraction access
Ticket access capabilities SHALL be interfaces implemented by the ticket classes: general access by every ticket, individual access by individual tickets, and group access by group tickets. Each attraction type SHALL be a class of a generic attraction type bounded by the capability it accepts. Its typed entry operations SHALL use the capability's methods, so the bound restricts which tickets the attraction can handle. `INDIVIDUAL` and `SPECIAL` attractions SHALL share the individual access capability, and the Special Pass rule SHALL belong to the special attraction's own behavior.

#### Scenario: Bounds reflect accepted tickets
- **WHEN** the attraction classes are inspected
- **THEN** the general, individual, group, and special attractions are parameterized with the general, individual, group, and individual access capabilities respectively
