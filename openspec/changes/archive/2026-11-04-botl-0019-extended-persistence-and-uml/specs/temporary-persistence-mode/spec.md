# Spec Delta

## MODIFIED Requirements

### Requirement: All persistence redirected
In `--fly` mode every persistent element — users, groups, tickets, group tickets, attractions (with their types) and passes — SHALL be read from and written to files inside the temporary directory only, using the same persistence components as a normal execution. The permanent files SHALL NOT be read or modified.

#### Scenario: Permanent data untouched
- **GIVEN** permanent files with user Ana
- **WHEN** a `--fly` execution creates Pedro and ends
- **THEN** the permanent files are byte-for-byte unchanged and the `--fly` run did not see Ana

#### Scenario: Commands operate on temporary data
- **WHEN** the application runs `--fly input_fase2.txt` where the file creates and then updates a user
- **THEN** the update succeeds because the created user is persisted in the temporary directory during the run

#### Scenario: Delivery 3 data isolated
- **WHEN** a `--fly` run creates an attraction of type `GROUP`, a ticket and SpeedPasses
- **THEN** the passes and typed attraction are written only inside the run directory and the permanent `passes.txt` and `attractions.txt` are unchanged
