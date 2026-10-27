# Spec Delta

## ADDED Requirements

### Requirement: Delivery 3 command coverage in help
HELP SHALL list the syntax of every Delivery 1, 2 and 3 command, including `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`, `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, the SPEEDPASS, SPECIALPASS and INVOICE commands, `HELP`, `HELP EXTENDED` and `EXIT`, and HELP EXTENDED SHALL give a description for each.

#### Scenario: Required syntax present
- **WHEN** `HELP` and `HELP EXTENDED` are processed
- **THEN** every required syntax line appears in both, each followed by a description in the extended output

### Requirement: Delivery 3 reference flow
A command file mixing Delivery 1, 2 and 3 commands SHALL produce identical output when run with `java -jar target/nombre-aplicacion.jar input_fase3.txt` and with `--fly input_fase3.txt`, the latter leaving no data behind.

#### Scenario: Mixed Phase 3 file in both modes
- **WHEN** the same Phase 3 command file runs once normally on an empty data directory and once with `--fly`
- **THEN** both outputs are identical, include pass consumption and invoices, and only the normal run leaves data files (with consumed passes still stored)

### Requirement: Delivery 3 technical restrictions
The application SHALL keep the earlier restrictions with BCrypt as the only non-JDK runtime library, and consumed passes SHALL never be deleted as a consequence of their use.

#### Scenario: Restrictions verified
- **WHEN** the production sources and the stored data after consuming passes are inspected
- **THEN** only `java.*`, project and `org.mindrot.jbcrypt` imports are used, and consumed passes are still stored
