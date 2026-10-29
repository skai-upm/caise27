# Spec Delta

## ADDED Requirements

### Requirement: Command file processing
When started with a file path as first argument, the application SHALL process the file's lines as commands, one per line, in the order in which they appear, ignoring blank lines, through the same processing used for interactive input; a nonexistent or unreadable file SHALL be reported with an error message without an uncontrolled failure. In interactive mode the application SHALL keep waiting for the next command after processing each one.

#### Scenario: Source example sequence from a file
- **WHEN** a file contains `USER CREATE` lines for Ana and Pedro, `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999` and `EXIT`, with blank lines between them
- **THEN** both users are created, Ana's phone is updated, blank lines produce no output, and the output equals the output of typing the same commands interactively

#### Scenario: Unreadable file
- **WHEN** the first argument names a directory instead of a readable file
- **THEN** an error message is shown and the application ends normally

#### Scenario: Interactive mode keeps waiting
- **WHEN** commands are typed one by one without EXIT and the input then ends
- **THEN** every command is processed in turn and the application ends normally at the end of input
