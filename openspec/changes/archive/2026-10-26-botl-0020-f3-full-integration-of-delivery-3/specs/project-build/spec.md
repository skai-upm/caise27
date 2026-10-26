# Spec Delta

## ADDED Requirements

### Requirement: Delivery command scripts
The repository SHALL include command scripts for Delivery 1, 2 and 3 (`examples/input_basico.txt`, `examples/input_fase2.txt`, `examples/input_fase3.txt`) that run with the packaged JAR in file mode, with and without `--fly`, without `Unknown command` or wrong-syntax errors.

#### Scenario: Run Delivery 3 script with --fly
- **WHEN** `java -jar target/parque.jar --fly examples/input_fase3.txt` is executed after running the earlier scripts' setup within the same script
- **THEN** every command is recognised, the expected purchases, entries and invoices are printed, and the temporary directory is removed at the end
