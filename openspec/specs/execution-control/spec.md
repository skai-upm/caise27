# execution-control Specification

## Purpose

Defines the general commands that control the application's execution: `HELP`, which lists the available commands and their syntax, and `EXIT`, which ends the application in a controlled way.

## Requirements

### Requirement: HELP command
The `HELP` command SHALL display the available commands and their basic syntax, including at least, in this order, `USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>`, `USER DELETE;<Email>;<Password>`, `USER UPDATE;<Email>;<Password>;<Field>;<NewValue>`, `SYSTEM DATE;<yyyy-MM-dd>`, `SYSTEM PRICE;<Price>`, `GROUP CREATE;<Identifier>;<Name>`, `GROUP ADD;<Identifier>;<Email>`, `GROUP REMOVE;<Identifier>;<Email>`, `GROUP DELETE;<Identifier>`, `TICKET CREATE;<identifier>;<email>;<date>`, `TICKET DELETE;<identifier>`, `GROUPTICKET CREATE;<identifier>;<group>;<date>`, `GROUPTICKET DELETE;<identifier>`, `ATTRACTION CREATE;<identifier>;<name>;<capacity>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION EXIT;<attraction>;<email>`, `ATTRACTION LIST;<attraction>`, `ATTRACTION STATUS;<attractionIdentifier>`, `HELP` and `EXIT`, and SHALL NOT change any data.

#### Scenario: Help in interactive mode
- **WHEN** `HELP` is entered on standard input
- **THEN** the syntax of USER CREATE, USER DELETE, USER UPDATE, HELP and EXIT is displayed and the application keeps waiting for commands

#### Scenario: Help in file mode
- **GIVEN** a command file containing `HELP`
- **WHEN** the file is processed
- **THEN** the same help text is displayed

#### Scenario: Complete Delivery 2 syntax listed in order
- **WHEN** `HELP` is processed
- **THEN** each of the listed syntax lines appears exactly as written and in the listed order

### Requirement: EXIT command
The `EXIT` command SHALL terminate the application in a controlled manner with exit status 0. Once `EXIT` is processed, no subsequent command MUST be executed, whether commands come from standard input or from a file.

#### Scenario: Exit in interactive mode
- **WHEN** `EXIT` is entered on standard input
- **THEN** the application terminates with exit status 0 without waiting for more input

#### Scenario: No command after EXIT in file mode
- **GIVEN** a command file with a `USER CREATE` for Ana, then `EXIT`, then a `USER CREATE` for Pedro
- **WHEN** the file is processed
- **THEN** Ana is created, the application terminates, and Pedro is not created

#### Scenario: General commands take no arguments
- **WHEN** `EXIT;now` or `HELP;x` is processed
- **THEN** an error message starting with `ERROR` is printed and the application continues

### Requirement: HELP EXTENDED command
`HELP EXTENDED` SHALL display every available command with its syntax and a brief explanation of its purpose, and SHALL NOT change any data. `HELP` SHALL continue to display the syntax of the recognized commands.

#### Scenario: Extended help lists purpose with syntax
- **WHEN** `HELP EXTENDED` is processed
- **THEN** for every command shown by `HELP` the same syntax appears together with a non-empty purpose text, for example `USER CREATE;<FirstName>;...` followed by an explanation that it registers a new user

#### Scenario: HELP unchanged
- **WHEN** `HELP` is processed
- **THEN** the syntax lines are displayed without purpose explanations

#### Scenario: Extra elements rejected
- **WHEN** `HELP EXTENDED;x` is processed
- **THEN** an error message starting with `ERROR` is printed

### Requirement: Injected help generation behavior
The class responsible for generating help text SHALL receive, through dependency injection during initialization, the behavior used to build the output, so that new help variants can be added by supplying a new behavior without modifying command reading and execution.

#### Scenario: New help variant without changing command processing
- **GIVEN** a help generator constructed with a different output behavior
- **WHEN** it generates the help for the registered commands
- **THEN** the output follows that behavior and the command processor code is unchanged

### Requirement: Delivery 3 HELP content
`HELP` SHALL display the commands of all three deliveries, each listed syntax form on its own line, including at least, in this order, `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`, `ATTRACTION ENTER;<attraction>;<ticket>`, `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>`, `SPECIALPASS DELETE;<specialPassIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>`, `HELP`, `HELP EXTENDED` and `EXIT`, while the Delivery 1 and 2 syntax lines continue to appear. `HELP EXTENDED` SHALL describe the purpose of every command.

#### Scenario: Delivery 3 syntax lines in order
- **WHEN** `HELP` is processed
- **THEN** each listed Delivery 3 syntax appears on a line of its own, in the listed order, and the Delivery 2 list still appears in its order

#### Scenario: Extended help covers all commands
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every syntax line shown by `HELP` is accompanied by a purpose description
