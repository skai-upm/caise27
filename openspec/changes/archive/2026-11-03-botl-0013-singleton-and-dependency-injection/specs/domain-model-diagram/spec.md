# Spec Delta

## ADDED Requirements

### Requirement: Repositories and singletons in the diagram
The class diagram SHALL show the repository interfaces realised by the file repositories, the controllers depending on the interfaces, and each Singleton controller's reference to its unique instance.

#### Scenario: Repository interfaces shown
- **WHEN** the diagram is reviewed
- **THEN** `UserFileStorage` realises `UserRepository` (and likewise for groups, tickets, group tickets and attractions) and `UserController` is associated with `UserRepository`, not with the file class
