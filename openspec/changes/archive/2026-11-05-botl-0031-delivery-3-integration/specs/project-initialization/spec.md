## ADDED Requirements

### Requirement: Delivery 3 technical restrictions
Delivery 3 SHALL keep the compatible restrictions of previous deliveries and additionally: layered packages; invoices built with the Builder pattern; Singleton controllers; repositories that are not singletons and are configured and injected at initialization; BCrypt as a Maven dependency for passwords; bounded generics and access interfaces restricting attraction access; consumed passes kept (never deleted by use); atomic group SpeedPass entry; and no Java serialization or databases.

#### Scenario: Consumed passes are kept
- **GIVEN** a SpeedPass consumed by an entry
- **WHEN** the passes are listed or the application restarts
- **THEN** the consumed SpeedPass still exists, marked consumed

### Requirement: Delivery 3 end-to-end execution
After `mvn package`, the JAR SHALL run a command file combining commands of the three deliveries both normally and with `--fly`, producing the same responses from the same initial state, without `ERROR` lines for valid commands, and leaving no data after the `--fly` execution.

#### Scenario: Same Delivery 3 file, normal and fly
- **GIVEN** an empty working directory and `input_fase3.txt` with users, groups, typed attractions, tickets, group tickets, SpeedPasses, a Special Pass, SPEED and SPECIAL entries, invoices, HELP EXTENDED and EXIT
- **WHEN** it is run with `--fly input_fase3.txt` and then with `input_fase3.txt`
- **THEN** both outputs are identical without `ERROR` lines, the temporary directory is gone after the first run and only the second run leaves data files
