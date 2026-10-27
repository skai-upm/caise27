# Design

## Decisions

- **HELP text** rewritten line by line with the assignment's exact placeholders; the field list for USER UPDATE stays as an extra hint line. Tests checking the older lowercase Phase 1 placeholders are updated.
- **`Delivery2IntegrationTest`** writes `input_fase2.txt` with dates relative to today, runs `Main.run` normally and with `--fly` in separate temporary base directories, compares outputs, and checks the data files exist only for the normal run.
- **Restriction test** inspects declared field types of `User` and `Group` via reflection.
