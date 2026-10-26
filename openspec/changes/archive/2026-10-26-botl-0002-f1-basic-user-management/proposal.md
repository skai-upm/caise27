# Proposal

## Why

The project is an empty quickstart skeleton. The first functional epic must let the park manage its users from the terminal through text commands, with validated data, uniqueness by DNI and email, and file persistence so users survive restarts. Everything later (groups, tickets, attractions) depends on registered users.

## What Changes

- The application reads text commands from the terminal (standard input), one per line, and interprets each with `String.split` on `;`.
- New commands:
  - `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta`
  - `USER DELETE;Correo;Contraseña`
  - `USER UPDATE;Correo;Contraseña;Campo;NuevoValor` with editable fields `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE`, `CARD` represented by an enum.
- Validation without regular expressions or external libraries: Spanish DNI (8 digits + check letter), email (`texto@texto.xxx`), password (upper, lower, digit, symbol), card (exactly 16 digits).
- Uniqueness of DNI and email across users; DNI is immutable.
- Users persisted in a semicolon-separated text file (one user per line) and reloaded at startup.
- Invalid commands or missing parameters are reported without terminating the program.
- Explicitly out of scope: groups, tickets, attractions, passes, encryption, external libraries, regex, databases, frameworks, HELP/EXIT and file input mode.

## Capabilities

### New Capabilities
- `command-interface`: How the application receives text commands from the terminal, splits them into operation and parameters, and reports results and errors without crashing.
- `user-management`: Registering, deleting and updating park users, including data validation, uniqueness rules, DNI immutability and persistence of users between executions.

### Modified Capabilities
<!-- None: project-build requirements are unchanged (mvn test must still pass). -->

## Impact

- New classes in `es.upm.parque`: user model, editable-field enum, validator, user management logic, file-based user repository, command processor; `App` becomes the terminal entry point.
- New runtime data file `data/users.txt` (created on first save); `data/` added to `.gitignore`.
- New unit tests; `mvn test` must keep passing. No new dependencies.
