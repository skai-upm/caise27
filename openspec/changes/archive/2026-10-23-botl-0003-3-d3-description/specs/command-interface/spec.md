# Spec Delta

## MODIFIED Requirements

### Requirement: Semicolon-separated command syntax
Each command SHALL be one line of `;`-separated elements. The first element is the command keyword: one of `USER CREATE|DELETE|UPDATE`, `SYSTEM DATE|PRICE`, `GROUP CREATE|ADD|REMOVE|DELETE`, `TICKET CREATE|DELETE`, `GROUPTICKET CREATE|DELETE`, `SPEEDPASS CREATE|DELETE`, `SPECIALPASS CREATE|DELETE`, `ATTRACTION CREATE|ENTER|EXIT|LIST|STATUS`, `INVOICE CREATE`, `HELP`, `HELP EXTENDED`, and `EXIT`. Whitespace around elements SHALL be ignored, and keywords matched case-insensitively. Parsing SHALL use only basic `String` operations and `split`.

#### Scenario: Recognized command with arguments
- **WHEN** the line `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** it is interpreted as the `USER DELETE` command with arguments `ana@correo.com` and `Clave1!`

#### Scenario: Surrounding whitespace is ignored
- **WHEN** the line `  help  ` is processed
- **THEN** it is interpreted as the `HELP` command

#### Scenario: Delivery 2 keyword recognized
- **WHEN** the line `attraction status;A01` is processed
- **THEN** it is interpreted as the `ATTRACTION STATUS` command with argument `A01`

#### Scenario: Delivery 3 keyword recognized
- **WHEN** the line `speedpass create;SP01;T001;1` is processed
- **THEN** it is interpreted as the `SPEEDPASS CREATE` command with arguments `SP01`, `T001`, and `1`

### Requirement: HELP command
`HELP` SHALL print the syntax of every command of all deliveries. That includes every Delivery 1 and 2 syntax, `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`, `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`, `SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>`, `SPEEDPASS DELETE;<speedPassIdentifier>`, `SPECIALPASS CREATE;<identifier>;<individualTicket>`, `SPECIALPASS DELETE;<specialPassIdentifier>`, `INVOICE CREATE;USER;<email>`, `INVOICE CREATE;GROUP;<groupIdentifier>`, and `HELP EXTENDED`.

#### Scenario: Help lists all commands
- **WHEN** `HELP` is processed
- **THEN** the output contains the syntax of `USER CREATE`, `USER DELETE`, `USER UPDATE`, `HELP`, and `EXIT`

#### Scenario: Help lists Delivery 2 commands
- **WHEN** `HELP` is processed
- **THEN** the output also contains the syntax of every `SYSTEM`, `GROUP`, `TICKET`, `GROUPTICKET`, and `ATTRACTION` command

#### Scenario: Help lists Delivery 3 commands
- **WHEN** `HELP` is processed
- **THEN** the output also contains every `SPEEDPASS`, `SPECIALPASS`, and `INVOICE CREATE` syntax, the typed `ATTRACTION CREATE`, `ATTRACTION ENTER` with `SPEED`, and `HELP EXTENDED`

## ADDED Requirements

### Requirement: HELP EXTENDED command
`HELP EXTENDED` SHALL print, for every recognized command syntax, the syntax together with a short description of the command's purpose. It SHALL change no state.

#### Scenario: Extended help describes commands
- **WHEN** `HELP EXTENDED` is processed
- **THEN** every syntax listed by `HELP` appears, each followed by a non-empty description
