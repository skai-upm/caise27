# Spec Delta

## ADDED Requirements

### Requirement: HELP EXTENDED command
`HELP EXTENDED` SHALL display, from the same command registry, every command's syntax followed by a brief description of its behavior. The help text SHALL be produced by a help generator that receives its output format through dependency injection, so new formats can be added without changing command processing.

#### Scenario: Syntax and descriptions
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every command shown by `HELP` is shown with its syntax and a description

#### Scenario: Same registry for both modes
- **WHEN** a command is registered
- **THEN** it appears in both `HELP` and `HELP EXTENDED` output without further changes

## MODIFIED Requirements

### Requirement: HELP command
The `HELP` command SHALL display, from the registry of available commands, every command with its basic syntax and no description: the F1 user commands, `SYSTEM DATE`, `SYSTEM PRICE`, the `GROUP`, `TICKET`, `GROUPTICKET`, `ATTRACTION`, `SPEEDPASS`, `SPECIALPASS`, and `INVOICE` commands, `HELP`, `HELP EXTENDED`, and `EXIT`.

#### Scenario: Help lists current commands
- **WHEN** `HELP` is processed
- **THEN** every command listed in this requirement is displayed with its syntax

#### Scenario: F3 commands listed
- **WHEN** `HELP` is processed
- **THEN** `SPEEDPASS CREATE`, `SPEEDPASS DELETE`, `SPECIALPASS CREATE`, `SPECIALPASS DELETE`, `INVOICE CREATE`, and `HELP EXTENDED` appear with their syntax
