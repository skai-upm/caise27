# Spec Delta

## MODIFIED Requirements

### Requirement: Shared processing
Interactive and file modes SHALL use the same command loop and the same command processor, so the same sequence of commands yields the same printed results and persisted effects from either source.

#### Scenario: Equivalent runs
- **GIVEN** two fresh users files
- **WHEN** the same command sequence is run once through standard input and once through a command file
- **THEN** the printed results are identical and both users files have identical contents except for the salted password hashes, which verify the same passwords
