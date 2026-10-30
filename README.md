# caise27

## Amusement Park (Deliveries 1-3)

Java 21 / Maven console application that manages users through text commands.

### Build

```bash
mvn test       # compile and run the tests
mvn package    # build target/amusement-park-1.0-SNAPSHOT.jar
```

### Run

```bash
java -jar target/amusement-park-1.0-SNAPSHOT.jar                    # interactive mode (standard input)
java -jar target/amusement-park-1.0-SNAPSHOT.jar input_basico.txt   # file mode (one command per line)
java -jar target/amusement-park-1.0-SNAPSHOT.jar --fly              # interactive, temporary storage
java -jar target/amusement-park-1.0-SNAPSHOT.jar --fly input.txt    # file mode, temporary storage
```

Both modes use the same command processing. Blank lines in files are ignored and `EXIT` stops
processing immediately. Data is stored in the working directory and reloaded at startup:
`users.txt`, `groups.txt`, `tickets.txt`, `grouptickets.txt`, `passes.txt` and `attractions.txt`
(fields separated by `;`). Passwords are stored as BCrypt hashes. Users present in attractions are not
stored: attractions start empty.

With `--fly` every file is written to a new `tmp/yyyyMMdd_HHmmss/` directory that is deleted when
the run ends, so permanent data is never modified.

The system date starts as today's date and the base ticket price as `40.00`; neither is stored.
Group tickets get a discount by size: 3-5 members 10 %, 6-10 members 20 %, more than 10 members 50 %.

### Commands

```text
USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta
USER DELETE;Correo;Contraseña
USER UPDATE;Correo;Contraseña;Campo;NuevoValor   (Campo: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)
SYSTEM DATE;yyyy-MM-dd
SYSTEM PRICE;Precio
GROUP CREATE;IdGrupo;Nombre
GROUP ADD;IdGrupo;Correo
GROUP REMOVE;IdGrupo;Correo
GROUP DELETE;IdGrupo
TICKET CREATE;IdTicket;Correo;Fecha
TICKET DELETE;IdTicket
GROUPTICKET CREATE;IdTicket;IdGrupo;Fecha
GROUPTICKET DELETE;IdTicket
ATTRACTION CREATE;IdAtraccion;Nombre;Aforo[;Tipo]   (Tipo: GENERAL, INDIVIDUAL, GROUP, SPECIAL)
ATTRACTION ENTER;IdAtraccion;IdTicket[;SPEED]
ATTRACTION EXIT;IdAtraccion;IdTicket
ATTRACTION STATUS;IdAtraccion
INVOICE CREATE;USER;Correo
INVOICE CREATE;GROUP;IdGrupo
SPEEDPASS CREATE;IdTicket;Cantidad   (Cantidad: 1 or 10)
SPEEDPASS DELETE;IdPase
SPECIALPASS CREATE;IdTicket
SPECIALPASS DELETE;IdPase
HELP
HELP EXTENDED
EXIT
```

Passes: a SpeedPass costs 20 % of its ticket's price and lets one user enter with `;SPEED`; the Special Pass
costs the ticket's price, at most one per ticket, and is consumed automatically at SPECIAL attractions.
GENERAL attractions accept individual and group tickets, INDIVIDUAL only individual tickets, GROUP only group
tickets and SPECIAL only individual tickets with an unused Special Pass.

Code is organised in `presentation`, `business` and `persistence` packages under `es.upm.amusementpark`.

The class model is documented in [`uml/model.puml`](uml/model.puml).
