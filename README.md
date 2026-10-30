# caise27

## Amusement Park (Deliveries 1-2)

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
`users.txt`, `groups.txt`, `tickets.txt`, `grouptickets.txt` and `attractions.txt`
(fields separated by `;`). Users present in attractions are not stored: attractions start empty.

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
ATTRACTION CREATE;IdAtraccion;Nombre;Aforo
ATTRACTION ENTER;IdAtraccion;IdTicket
ATTRACTION EXIT;IdAtraccion;IdTicket
ATTRACTION STATUS;IdAtraccion
HELP
EXIT
```

The class model is documented in [`uml/model.puml`](uml/model.puml).
