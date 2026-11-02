# Spec Delta

## Purpose

Allows commands to come from standard input or from a file named on the command line, with identical observable behavior because both sources feed the same command processing.

## ADDED Requirements

### Requirement: Interactive mode
When started without arguments, the application SHALL read commands from standard input one line at a time and, after processing each command, wait for the next one until `EXIT` or end of input. (F1-C07)

#### Scenario: Interactive sequence
- **WHEN** the lines `USER CREATE;Ana;...`, `USER UPDATE;ana@correo.com;Clave1!;PHONE;699999999`, `HELP` and `EXIT` are entered on standard input
- **THEN** each command is processed in turn and the application then terminates

### Requirement: File mode
When started with a path as its first argument, the application SHALL process the file's commands, one per line, in the order they appear, ignoring blank lines. (F1-C07)

#### Scenario: File sequence in order
- **WHEN** a file containing two `USER CREATE` lines, a blank line and a `USER UPDATE` line is processed
- **THEN** the commands run in file order, the blank line is skipped, and the result equals running the same commands interactively

### Requirement: Single shared command processing
Interactive and file input SHALL be handled by the same parser and executor, so a command's observable behavior is the same regardless of its source. (F1-C07)

#### Scenario: Equivalent results
- **WHEN** the same command sequence is executed interactively and from a file, from equivalent initial states
- **THEN** both produce the same output and the same resulting user state

### Requirement: Controlled handling of unreadable files
A nonexistent or unreadable command file SHALL be reported as an error without an uncaught exception or stack trace. (F1-C07)

#### Scenario: Missing file
- **WHEN** the application is started with a path that does not exist
- **THEN** an error message naming the file is shown and the application ends in a controlled way
