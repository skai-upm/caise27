# caise27 — Amusement Park

Terminal application for managing an amusement park, built with Maven and Java 21 (standard library only).

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

Users are stored in `data/users.txt` (relative to the working directory), one user per line with fields
separated by `;`, and are loaded again on every start.

## Commands

| Command | Description |
|---------|-------------|
| `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta` | Create a user after validating all data |
| `USER DELETE;Correo;Contraseña` | Delete a user (password must match) |
| `USER UPDATE;Correo;Contraseña;Campo;NuevoValor` | Update `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` or `CARD` (the DNI cannot change) |
| `HELP` | Show the available commands |
| `EXIT` | Stop processing commands |

Successful commands print `OK ...`; invalid commands or data print `ERROR ...` and the application keeps running.
