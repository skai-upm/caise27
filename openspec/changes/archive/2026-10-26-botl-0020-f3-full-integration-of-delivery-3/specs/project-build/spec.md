# Spec Delta

## ADDED Requirements

### Requirement: End-to-end delivery integration check
The build SHALL include an automated test that runs a command script covering E1, E2 and E3 commands through the application entry point in file mode, interactive mode and `--fly` mode, and checks the responses, the persistence files of normal mode and the cleanup of `--fly` mode.

#### Scenario: Integration script
- **WHEN** `mvn test` runs
- **THEN** the integration script is executed in all three modes and every expected response is verified

#### Scenario: Packaged application
- **WHEN** `mvn package` is run and `java -jar target/parque.jar --fly <script>` executes the integration script
- **THEN** it produces the same responses as the test run

### Requirement: Run documentation
The repository README SHALL explain how to build the executable JAR, the three execution modes (interactive, command file, `--fly`) and how to list the commands with `HELP` and `HELP EXTENDED`.

#### Scenario: README content
- **WHEN** `README.md` is read
- **THEN** it shows `mvn package`, `java -jar target/parque.jar`, `java -jar target/parque.jar <fichero>`, `--fly`, `HELP` and `HELP EXTENDED`
