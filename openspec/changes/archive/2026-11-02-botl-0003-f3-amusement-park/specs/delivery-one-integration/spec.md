# Spec Delta

## MODIFIED Requirements

### Requirement: Delivery 1 technical restrictions
Delivery 1 SHALL use Java 21, the standard Maven structure, build with `mvn package` into an executable JAR, run interactively and with a `.txt` command file, use only the Java standard library at runtime, persist to files, interpret commands with basic String operations and `split`, keep the user, main and persistence classes separate, and encapsulate attributes. It SHALL NOT use databases, frameworks, external libraries, regular expressions or encryption, except that from Delivery 3 the BCrypt library is used to protect passwords. (F1-C09, F3-C05)

#### Scenario: Fresh build
- **WHEN** `mvn package` runs on a fresh checkout
- **THEN** `target/nombre-aplicacion.jar` is produced and runs independently of an IDE

#### Scenario: Dependency inspection
- **WHEN** the runtime dependencies of the build are inspected
- **THEN** none are declared beyond the Java standard library, except the BCrypt dependency added in Delivery 3
