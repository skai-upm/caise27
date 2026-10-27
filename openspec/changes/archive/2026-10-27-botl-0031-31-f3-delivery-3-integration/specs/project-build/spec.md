# Spec Delta

## ADDED Requirements

### Requirement: Delivery 3 technical restrictions
The application SHALL keep the compatible restrictions of previous deliveries and SHALL use a layered package organisation, the Builder pattern for invoices, Singleton for controllers with unique management responsibilities, non-singleton repositories configured and injected at initialization, BCrypt as a Maven dependency for passwords, bounded generics and interfaces to restrict attraction access, consumed passes kept after use, and atomic group entries consuming several SpeedPasses.

#### Scenario: Restrictions verified automatically
- **WHEN** the test suite runs
- **THEN** architecture, dependency-injection, generics, builder, password and integration tests covering each restriction pass

#### Scenario: Delivered JAR
- **WHEN** a fresh clone is built with `mvn package` and `java -jar target/amusement-park.jar input_fase3.txt` and `java -jar target/amusement-park.jar --fly input_fase3.txt` are run
- **THEN** both executions process the file and the `--fly` run leaves no data behind
