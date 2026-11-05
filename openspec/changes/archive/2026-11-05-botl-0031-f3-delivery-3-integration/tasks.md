## 1. HELP consolidation

- [x] 1.1 Split and reorder the `CommandCatalog` entries:
  - literal `ATTRACTION CREATE …;<type>` and `ATTRACTION ENTER …;SPEED` syntaxes;
  - pass commands in section 15 order;
  - 29 entries, each with a purpose.

  Update the tests that pin the catalog size.

## 2. Integration verification

- [x] 2.1 Add `Delivery3IntegrationTest`: a combined Delivery 1+2+3 command file through `App.execute` in normal mode with a restart and in `--fly` mode, checking identical responses, no leftovers and untouched normal files.
- [x] 2.2 Add a README integration section with the Delivery 3 run commands.
- [x] 2.3 Run `mvn package`, the full test suite and an end-to-end JAR regression: identical to the baseline except for the HELP lines, plus a Delivery 3 file run in normal and `--fly` modes.
