# Spec Delta

## ADDED Requirements

### Requirement: Temporary mode covers all data
In `--fly` mode every persistent element, including personal passes and attraction types, SHALL be stored in the temporary directory and never in `data/`.

#### Scenario: Passes in fly mode
- **WHEN** a `--fly` execution buys passes and creates a SPECIAL attraction
- **THEN** `passes.txt` and `attractions.txt` are written only inside the temporary directory, which is deleted at the end
