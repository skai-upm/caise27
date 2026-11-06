# Spec Delta

## Purpose

Defines how the Amusement Park application is assembled: which controllers are single instances, how replaceable repositories are created and injected at initialization, and how controllers are wired to collaborate.

## ADDED Requirements

### Requirement: Singleton controllers
The central park controller and the controllers that manage users, groups, tickets, attractions and invoices SHALL each have exactly one instance per execution, obtained only through their single access point; none of them can be instantiated elsewhere.

#### Scenario: One instance per controller
- **WHEN** a controller is requested several times from different components
- **THEN** the same instance is returned each time, and its constructor is not accessible from other classes

### Requirement: Repositories injected as replaceable dependencies
Repositories MUST NOT be singletons. They SHALL be created and configured during application initialization — from the permanent data directory, from the `--fly` temporary directory, or in memory — and injected into the controllers that need them, so the same controller logic works with any repository configuration.

#### Scenario: Different configurations, same controllers
- **GIVEN** one repository configuration loaded from directory `D1` and another from directory `D2`
- **WHEN** the park is started with the first and later with the second
- **THEN** the controllers operate on the data of `D1` and then of `D2`, without any change to controller code, and each configuration has its own repository instances

#### Scenario: Fly and normal modes differ only by configuration
- **WHEN** the application starts with and without `--fly`
- **THEN** the only difference is the directory used to build the injected repositories

### Requirement: Collaborations wired at initialization
Dependencies between controllers SHALL be established once when the park is started, not created inside individual operations; controllers use the dependencies injected at start-up for every operation.

#### Scenario: Group locking uses the injected ticket controller
- **GIVEN** the park was started with a repository configuration containing an active group ticket for `G01`
- **WHEN** `GROUP ADD;G01;luis@correo.com` is processed
- **THEN** the group controller consults the ticket controller wired at start-up and rejects the operation

### Requirement: No dependency-injection framework
Injection SHALL be implemented with plain Java code of the project, without dependency-injection frameworks.

#### Scenario: No framework dependency
- **WHEN** the project dependencies are inspected
- **THEN** no dependency-injection framework is declared
