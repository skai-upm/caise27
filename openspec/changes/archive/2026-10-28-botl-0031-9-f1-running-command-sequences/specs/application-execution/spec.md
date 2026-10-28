## ADDED Requirements

### Requirement: Identical processing for interactive and file input
Commands read from standard input and from a command file SHALL be processed by the same command processing, producing the same output and state; in a file, commands SHALL run one per line in file order, blank lines SHALL be ignored, and a nonexistent or unreadable file SHALL be reported with an error message without abnormal termination.

#### Scenario: Delivery 1 example in both modes
- **WHEN** the lines `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`, `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999`, `HELP` and `EXIT` are given once on standard input and once as a command file (each on empty data)
- **THEN** both runs produce the same output and leave a user with telephone `699999999`

#### Scenario: Blank lines and order
- **WHEN** a command file contains blank lines between `USER CREATE` lines for Ana and Pedro, then `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` and `EXIT`
- **THEN** the blank lines produce no output, both users are created in order and Ana's telephone is updated

#### Scenario: Unreadable file
- **WHEN** the application is started with a path that is a directory instead of a file
- **THEN** an error message about the command file is displayed and the application ends normally
