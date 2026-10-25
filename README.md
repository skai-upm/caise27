# Parque

Amusement Park management application (Java 21, Maven). The only runtime dependency is jBCrypt,
used to hash passwords; `mvn package` bundles it into the executable JAR.

## Build

```bash
mvn test       # compile and run the tests
mvn package    # build target/parque.jar
```

## Run

```bash
java -jar target/parque.jar                     # interactive mode: one command per line on standard input
java -jar target/parque.jar input_basico.txt    # file mode: commands read line by line from the file
java -jar target/parque.jar --fly               # interactive mode with temporary persistence
java -jar target/parque.jar --fly input_fase2.txt
```

Both modes use the same command processing. Blank lines are ignored and processing stops at `EXIT`
(or at the end of the input). Data is stored in `data/` (relative to the working directory:
`users.txt`, `groups.txt`, `tickets.txt`, `grouptickets.txt`, `passes.txt`, `attractions.txt`) and restored
at startup. Passwords are stored as BCrypt hashes.
Attractions always start empty.

With `--fly`, all data files are kept in a new `tmp/<yyyyMMdd_HHmmss>/` directory that is deleted when the
run ends, so the permanent data in `data/` is never read or modified.

The current date starts as the operating system date and the individual ticket price as 40.00; both can be
changed with `SYSTEM DATE` and `SYSTEM PRICE` and are not persisted.

## Commands

| Command | Description |
|---------|-------------|
| `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta` | Create a user |
| `USER DELETE;Correo;Contraseña` | Delete a user |
| `USER UPDATE;Correo;Contraseña;Campo;NuevoValor` | Update `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` or `CARD` |
| `SYSTEM DATE;yyyy-MM-dd` | Set the simulated current date |
| `SYSTEM PRICE;Precio` | Set the individual ticket price |
| `GROUP CREATE;IdGrupo;Nombre` | Create a group |
| `GROUP ADD;IdGrupo;Correo` / `GROUP REMOVE;IdGrupo;Correo` | Add / remove a registered user |
| `GROUP DELETE;IdGrupo` | Delete a group |
| `TICKET CREATE;IdEntrada;Correo;yyyy-MM-dd` | Buy an individual ticket (`OK TICKET CREATED -> <Precio>`) |
| `TICKET DELETE;IdEntrada` | Delete a ticket dated after the current date |
| `GROUPTICKET CREATE;IdEntrada;IdGrupo;yyyy-MM-dd` | Buy a group ticket (discount 0/10/20/50 % for <3, 3–5, 6–10, >10 members) |
| `GROUPTICKET DELETE;IdEntrada` | Delete a group ticket dated after the current date, with its internal tickets |
| `ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad[;Tipo]` | Create an attraction: `GENERAL` (default), `INDIVIDUAL`, `GROUP` or `SPECIAL` |
| `ATTRACTION DELETE;IdAtraccion` | Delete an empty attraction |
| `ATTRACTION ENTER;IdAtraccion;IdEntrada[;SPEED]` | Enter with a ticket valid today; `SPEED` consumes one SpeedPass per ticket; SPECIAL attractions consume the Special Pass |
| `ATTRACTION EXIT;IdAtraccion;IdEntrada` | Leave the attraction |
| `ATTRACTION STATUS;IdAtraccion` | Show capacity, occupancy and users present |
| `SPEEDPASS CREATE;IdEntrada;Cantidad` | Buy 1 or 10 SpeedPasses for an individual ticket (20 % of its price each) |
| `SPEEDPASS DELETE;IdPase` | Refund an unused SpeedPass of a ticket dated after the current date |
| `SPECIALPASS CREATE;IdEntrada` | Buy the Special Pass of an individual ticket (one per ticket, same price as the ticket) |
| `SPECIALPASS DELETE;IdPase` | Refund an unused Special Pass of a ticket dated after the current date |
| `INVOICE CREATE;USER;Correo` / `INVOICE CREATE;GROUP;IdGrupo` | Show an invoice (not stored) |
| `HELP` | Show the available commands |
| `HELP EXTENDED` | Show the commands with a description |
| `EXIT` | Terminate the application |

Validation rules: DNI must be 8 digits plus the correct check letter; email must look like
`texto@texto.xxx` (3-character extension); password needs an uppercase letter, a lowercase letter,
a digit and a symbol; card number must be exactly 16 digits. DNI and email must be unique.

Rules: a user with a ticket dated on or after the current date cannot be deleted; a group with a group ticket
dated on or after the current date cannot be modified or deleted.

## Code structure

- `es.upm.parque.App`: entry point and composition root (creates repositories, configures the singleton controllers).
- `es.upm.parque.presentation`: command registry and processing, input loop, help generation.
- `es.upm.parque.business`: domain model, controllers, invoice builder, validation and password hashing.
- `es.upm.parque.persistence`: text-file repositories.

## Design model

The class diagram is in [`uml/model.puml`](uml/model.puml).
