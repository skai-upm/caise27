# Spec Delta

## MODIFIED Requirements

### Requirement: Executable JAR build
The project SHALL be a standard Maven project targeting Java 21 that produces an executable JAR with `mvn package`, runnable with `java -jar` without an IDE or extra configuration. At runtime it SHALL use only the Java standard library plus the BCrypt library, which SHALL be declared as a Maven dependency and bundled inside the JAR.

#### Scenario: Build and run the JAR
- **WHEN** `mvn package` is run on a clean checkout
- **THEN** the build succeeds and produces `target/amusement-park.jar`
- **AND** `java -jar target/amusement-park.jar` starts the application

#### Scenario: Self-contained JAR
- **WHEN** only `target/amusement-park.jar` is copied to another directory and run with `java -jar`
- **THEN** creating a user (which hashes the password) works

### Requirement: Temporary persistence mode
When `--fly` is the first argument, the application SHALL create a new, empty directory `tmp/<yyyyMMdd_HHmmss>` (made unique if it already exists) and use it as the data directory for all persistent elements (users, groups, tickets, passes, attractions) during that execution. When execution ends — through `EXIT`, end of the command file or end of standard input — the directory and all its files SHALL be deleted, together with `tmp/` if it was created by this execution and is left empty. Without `--fly`, the normal data directory is used and argument handling is unchanged.

#### Scenario: Persistent data untouched
- **WHEN** `users.txt` exists in the working directory and the JAR is run with `--fly` and a file that creates users
- **THEN** the users are created during that run, the existing `users.txt` is unchanged, and no temporary files remain after it ends

#### Scenario: Fresh state
- **WHEN** the JAR is run with `--fly`
- **THEN** it starts with no users, groups, tickets, passes or attractions
