# caise27 — Amusement Park

Terminal application for managing an amusement park, built with Maven and Java 21. The only runtime dependency is
jBCrypt, used to store passwords as BCrypt hashes; `mvn package` bundles it into the executable JAR.

## Build

```bash
mvn test        # compile and run the tests
mvn package     # build the executable JAR at target/parque.jar
```

## Run

Interactive mode (commands are read from standard input until `EXIT` or end of input):

```bash
java -jar target/parque.jar
```

File mode (the first argument is a text file with one command per line; blank lines are ignored and
processing stops at `EXIT`):

```bash
java -jar target/parque.jar input_basico.txt
```

Temporary mode: `--fly` stores every data file in a new `tmp/<yyyyMMdd_HHmmss>/` directory that is deleted when
the run ends, so the permanent data is never modified. It works in both modes:

```bash
java -jar target/parque.jar --fly
java -jar target/parque.jar --fly input_fase2.txt
```

Data is stored in `data/` (relative to the working directory) as `;`-separated text files — `users.txt`,
`groups.txt`, `tickets.txt`, `grouptickets.txt`, `passes.txt` and `attractions.txt` — and is loaded again on every start.
The system date (initially today's date) and the ticket price (initially 40.00) are not stored.

## Commands

| Command | Description |
|---------|-------------|
| `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta` | Create a user after validating all data |
| `USER DELETE;Correo;Contraseña` | Delete a user (password must match; not while holding a ticket dated on or after the system date) |
| `USER UPDATE;Correo;Contraseña;Campo;NuevoValor` | Update `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` or `CARD` (the DNI cannot change) |
| `SYSTEM DATE;yyyy-MM-dd` | Set the simulated current date used by every date rule |
| `SYSTEM PRICE;Precio` | Set the individual ticket price |
| `GROUP CREATE;Id;Nombre` | Create an empty group |
| `GROUP ADD;Id;Correo` / `GROUP REMOVE;Id;Correo` | Add or remove an existing user (not while the group has a current/future group ticket) |
| `GROUP DELETE;Id` | Delete a group (not while it has a current/future group ticket) |
| `TICKET CREATE;Id;Correo;yyyy-MM-dd` | Buy an individual ticket (`OK TICKET CREATED -> <price>`) |
| `TICKET DELETE;Id` | Delete a ticket dated after the system date |
| `GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd` | Buy a group ticket: one ticket `<Id>-<DNI>` per member, with 0/10/20/50 % discount for <3, 3–5, 6–10, >10 members |
| `GROUPTICKET DELETE;Id` | Delete a group ticket dated after the system date with its member tickets |
| `SPEEDPASS CREATE;IdEntrada;Cantidad` | Buy 1 or 10 SpeedPasses (`<IdEntrada>-SP<n>`, 20 % of the ticket price each) |
| `SPEEDPASS DELETE;IdPase` | Refund an unused SpeedPass of a ticket dated after the system date |
| `SPECIALPASS CREATE;IdEntrada` | Buy the only Special Pass (`<IdEntrada>-SPECIAL`) a ticket can ever have, at the ticket price |
| `SPECIALPASS DELETE;IdPase` | Refund an unused Special Pass of a ticket dated after the system date |
| `ATTRACTION CREATE;Id;Nombre;Capacidad[;Tipo]` / `ATTRACTION DELETE;Id` | Create (type `GENERAL` by default, `INDIVIDUAL`, `GROUP` or `SPECIAL`) or delete (when empty) an attraction |
| `ATTRACTION ENTER;IdAtraccion;IdEntrada[;SPEED]` | Enter with a ticket for the system date admitted by the attraction type (all or nobody); `SPEED` consumes one SpeedPass per user; SPECIAL attractions consume the Special Pass |
| `ATTRACTION EXIT;IdAtraccion;IdEntrada` | Leave an attraction |
| `ATTRACTION STATUS;IdAtraccion` | Show capacity, occupancy, available places and users present |
| `INVOICE CREATE;USER;Correo` / `INVOICE CREATE;GROUP;IdGrupo` | Show an invoice (user: direct tickets and personal passes; group: group tickets) |
| `HELP` / `HELP EXTENDED` | Show the command syntax / syntax plus descriptions |
| `EXIT` | Stop processing commands |

Successful commands print `OK ...`; invalid commands or data print `ERROR ...` and the application keeps running.

## Architecture

- `es.upm.parque` — `App` (entry point, execution modes, `--fly`) and `ParkApplication` (composition root: creates
  the repositories for `data/` or `tmp/<timestamp>/` and injects them and the collaborating controllers).
- `es.upm.parque.presentation` — command processing (`CommandProcessor`, `ConsoleRunner`), the command registry and
  help generation with injected formats.
- `es.upm.parque.business` — `model` (users, groups, tickets, passes, generic attractions), `controller`
  (Singleton controllers), `invoice` (Builder), `security` (BCrypt), `validation`.
- `es.upm.parque.persistence` — repository interfaces and their text-file implementations.

The class model is documented in [`uml/model.puml`](uml/model.puml).
