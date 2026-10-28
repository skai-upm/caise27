# Spec Delta

## ADDED Requirements

### Requirement: Singleton controllers
Each controller with a unique management responsibility during an execution — the park controller and the user, group, ticket, attraction and invoice controllers — SHALL apply the Singleton pattern: no accessible constructor and a single instance obtained through a static accessor.

#### Scenario: Unique controller instances
- **WHEN** any of these controllers is requested twice from different places
- **THEN** the same instance is returned and none of their constructors is accessible

### Requirement: Injected repositories
Repositories and file storages SHALL NOT be singletons. They SHALL be created during application initialization, configured for the selected mode (permanent data directory or temporary `--fly` directory), and injected into the controllers that use them; controllers SHALL NOT create repositories themselves.

#### Scenario: Fresh repositories per initialization
- **WHEN** the application is initialized twice in the same JVM (for example, normal mode and then `--fly`)
- **THEN** each initialization injects new repository instances configured with its own paths, while the controller instances stay the same

### Requirement: Collaborations configured at initialization
Collaborations between controllers (for example, the group controller asking the ticket controller whether a group has active group tickets) SHALL be established by injection during initialization, using project code only and no dependency-injection framework.

#### Scenario: Wiring in one place
- **WHEN** the initialization code is inspected
- **THEN** a single composition root creates the repositories and injects repositories and collaborating controllers into each controller
