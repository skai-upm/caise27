# Tasks

## 1. HELP

- [x] 1.1 Replace `COMMAND_SYNTAX` with the Delivery 2 §15 list in source order and update the existing HELP assertions to the new spellings; verify with a test asserting all twenty syntaxes appear

## 2. Integration

- [x] 2.1 Add `Delivery2IntegrationTest` covering every Delivery 2 command together (programmatic users), the 6-member 192.00 example, lock/unlock, deletions, entry/exit/status/list, restart reconstruction through `App.run`, and `--fly` with a command file leaving no data; verify it passes
- [x] 2.2 Run `mvn clean package` and verify `BUILD SUCCESS`; from a scratch directory run `java -jar <abs>/target/amusement-park.jar input_fase2.txt` and `--fly input_fase2.txt` with a Delivery 2 command file, verifying controlled output, that normal mode writes `data/`, and that `tmp/` is left empty after `--fly`
