# Spec Delta

## Purpose

Preserves the complete park state between runs in text files, without occupants, and offers a temporary `--fly` mode that runs the same repositories against a throwaway directory.

## ADDED Requirements

### Requirement: Park state persistence
Groups with their users, individual tickets with their user, group tickets with their constituent tickets and (while it exists) their group, and attractions SHALL be stored in text files and reconstructed at start-up, in components separate from the domain and main classes, without Java serialization, databases, frameworks or external libraries. (F2-C08)

#### Scenario: Restart restores relationships
- **WHEN** users, groups, individual and group tickets and attractions exist and the application restarts
- **THEN** groups contain the same users, tickets reference the same users, group tickets have the same composition, price and group, and attractions exist with the same name and capacity

### Requirement: Occupants are not persisted
The users currently inside attractions SHALL NOT be persisted; every attraction SHALL start each execution empty. (F2-C08)

#### Scenario: Attractions empty after restart
- **WHEN** users are inside an attraction and the application restarts
- **THEN** that attraction has no occupants

### Requirement: Historical records survive deletions
Tickets of a deleted user and group tickets of a deleted group SHALL keep the information needed to reconstruct them (identifier, date, price, composition and holder identity) across restarts. (F2-C08)

#### Scenario: Deleted user's historical ticket
- **WHEN** a user with only historical tickets is deleted and the application restarts
- **THEN** those tickets are reconstructed with their identifier, date, price and the former holder's identity

### Requirement: Temporary --fly mode
When `--fly` is the first argument, the application SHALL create a timestamped directory `tmp/<yyyyMMdd_HHmmss>/`, configure every repository (users, groups, tickets, attractions) to use only files inside it, and delete it with all its files when execution ends via EXIT or end of input; `--fly` MAY be followed by a command file path. Without `--fly`, argument handling SHALL remain as in Delivery 1. (F2-C08)

#### Scenario: Fly run leaves no data
- **WHEN** `java -jar target/nombre-aplicacion.jar --fly input_fase2.txt` runs to completion or EXIT
- **THEN** the temporary directory no longer exists and the normal data files are unchanged

#### Scenario: Interactive fly
- **WHEN** `java -jar target/nombre-aplicacion.jar --fly` is run and commands are typed until EXIT
- **THEN** commands operate on an empty temporary state and the directory is removed at exit

#### Scenario: Same repositories, different paths
- **WHEN** the code is inspected
- **THEN** normal and `--fly` execution use the same repository classes and differ only in the configured directory
