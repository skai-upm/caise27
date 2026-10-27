# Design

## Decisions

- **`Delivery3IntegrationTest`** (presentation tests): builds `input_fase3.txt` with dates relative to today, runs `Main.run` in two fresh base directories (normal and `--fly`), compares outputs and asserts key lines (group discount, SpeedPass consumption, Special Pass consumption, refund rejection of consumed pass, user and group invoices) plus file state.
- **HELP completeness** checked against the assignment's list in the same test.
- **README** documents layers, Delivery 3 commands and run modes.
