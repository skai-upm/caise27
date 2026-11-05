## MODIFIED Requirements

### Requirement: HELP lists available commands
The `HELP` command SHALL print the syntax of every command recognized by the application, including at least, in this order: `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>`, `HELP`, `HELP EXTENDED` and `EXIT`, without changing any stored data.

#### Scenario: HELP in interactive mode
- **WHEN** `HELP` is entered on standard input
- **THEN** the output contains each of the command syntaxes listed above, in that order
- **AND** the application keeps waiting for the next command

#### Scenario: HELP in file mode
- **WHEN** a command file contains `HELP`
- **THEN** the same help text as in interactive mode is printed and processing continues with the next line

## ADDED Requirements

### Requirement: HELP EXTENDED explains the purpose of each command
The `HELP EXTENDED` command SHALL print, for every command listed by `HELP` and in the same order, its syntax together with a brief explanation of its purpose, without changing any stored data. `HELP` SHALL keep printing only the syntax. `HELP EXTENDED` followed by any additional field SHALL be rejected with an `ERROR` response.

#### Scenario: Extended help shows syntax and purpose
- **WHEN** `HELP EXTENDED` is executed
- **THEN** the output contains every command syntax shown by `HELP`, in the same order
- **AND** each syntax is accompanied by a non-empty description of what the command does, for example `INVOICE CREATE;USER;<email>` is described as generating the invoice of a user

#### Scenario: Plain help is unchanged in nature
- **WHEN** `HELP` is executed
- **THEN** the output lists the command syntaxes without the purpose descriptions

#### Scenario: Extra fields are rejected
- **WHEN** `HELP EXTENDED;x` is executed
- **THEN** the response starts with `ERROR`

### Requirement: Help generation behaviour is injected
The class responsible for generating help text SHALL receive, through dependency injection, the behaviour used to build the output (a help formatting strategy), so that new ways of generating help can be added without modifying it. Help variants SHALL be registered during initialization, and the general logic that reads and executes commands SHALL NOT depend on any concrete help variant.

#### Scenario: A new help variant needs no change to command processing
- **GIVEN** a help generator built with a different, injected formatting behaviour and registered under a new help command
- **WHEN** that command is processed
- **THEN** the output is produced by the injected behaviour, and the command processor code is unchanged

#### Scenario: Generator uses the injected behaviour
- **WHEN** a help generator is created with a given formatting behaviour
- **THEN** the text it generates is exactly the output of that behaviour applied to the command catalog
