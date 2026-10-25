# caise27

Amusement Park terminal application (Java 21, Maven).

## Architecture

- `es.upm.parque.App` (entry point) and `ParkApplication` (composition root: creates the repositories for `data/` or the `--fly` directory and injects them into the controllers).
- `presentation`: command registry/processor, console loop, command modules and help formatters.
- `business`: domain model, Singleton controllers, invoice Builder, password hashing (BCrypt), validation and repository interfaces.
- `persistence`: text-file repositories, temporary storage for `--fly`.

The class model is documented in `uml/model.puml`.

## Build

```bash
mvn test       # compile and run the tests
mvn package    # build the executable JAR target/parque.jar (bundles the jBCrypt dependency)
```

## Run

Interactive mode (reads commands from standard input until `EXIT` or end of input):

```bash
java -jar target/parque.jar
```

File mode (first argument is a file with one command per line; blank lines are ignored and nothing after `EXIT` is executed):

```bash
java -jar target/parque.jar input_basico.txt
```

Temporary mode: add `--fly` (with or without a command file) to keep every persistence file in a new `tmp/<yyyyMMdd_HHmmss>/` directory that is deleted when the application ends; the permanent data is not touched:

```bash
java -jar target/parque.jar --fly input_fase2.txt
```

Data is stored in `data/` (relative to the working directory): `users.txt` (passwords as BCrypt hashes), `groups.txt`, `tickets.txt`, `group_tickets.txt`, `passes.txt` and `attractions.txt`. The system date (initially today) and the ticket price (initially 40.00) are not stored.

## Commands

| Command | Description |
|---|---|
| `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta` | Register a user |
| `USER DELETE;Correo;Contraseña` | Delete a user |
| `USER UPDATE;Correo;Contraseña;Campo;NuevoValor` | Update `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` or `CARD` |
| `SYSTEM DATE;yyyy-MM-dd` | Set the current date used by every date rule |
| `SYSTEM PRICE;Precio` | Set the individual ticket price |
| `GROUP CREATE;IdGrupo;Nombre` | Create a group |
| `GROUP ADD;IdGrupo;Correo` / `GROUP REMOVE;IdGrupo;Correo` | Add / remove a member (not while the group has a current or future group ticket) |
| `GROUP DELETE;IdGrupo` | Delete a group (same lock) |
| `TICKET CREATE;IdEntrada;Correo;yyyy-MM-dd` | Buy an individual ticket |
| `TICKET DELETE;IdEntrada` | Cancel an individual ticket dated after the current date |
| `GROUPTICKET CREATE;IdEntrada;IdGrupo;yyyy-MM-dd` | Buy a group ticket (one ticket per member, 0/10/20/50% discount for <3/3–5/6–10/>10 members) |
| `GROUPTICKET DELETE;IdEntrada` | Cancel a group ticket dated after the current date, with its member tickets |
| `ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad[;Tipo]` / `ATTRACTION DELETE;IdAtraccion` | Create (type GENERAL by default, or INDIVIDUAL, GROUP, SPECIAL) / delete (when empty) an attraction |
| `ATTRACTION ENTER;IdAtraccion;IdEntrada[;SPEED]` | Enter with a ticket for the current date admitted by the attraction type (all or nothing); `SPEED` consumes one SpeedPass per ticket; SPECIAL attractions consume the Special Pass |
| `ATTRACTION EXIT;IdAtraccion;IdEntrada` | Leave the attraction |
| `ATTRACTION STATUS;IdAtraccion` | Show capacity, occupancy and users present |
| `SPEEDPASS CREATE;IdEntrada;Cantidad` | Buy 1 or 10 SpeedPasses (20% of the ticket price each) |
| `SPEEDPASS DELETE;IdPase` | Refund an unused SpeedPass of a ticket dated after today |
| `SPECIALPASS CREATE;IdEntrada` | Buy the ticket's Special Pass (ticket price, one per ticket) |
| `SPECIALPASS DELETE;IdPase` | Refund an unused Special Pass of a ticket dated after today |
| `INVOICE CREATE;USER;Correo` / `INVOICE CREATE;GROUP;IdGrupo` | Show a user's or group's invoice (not stored) |
| `HELP` | List the available commands |
| `HELP EXTENDED` | List the commands with a description |
| `EXIT` | Terminate the application |
