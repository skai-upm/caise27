# Parque

Amusement Park management application (Java 21, Maven, standard library only).

## Build

```bash
mvn test       # compile and run the tests
mvn package    # build target/parque.jar
```

## Run

```bash
java -jar target/parque.jar                     # interactive mode: one command per line on standard input
java -jar target/parque.jar input_basico.txt    # file mode: commands read line by line from the file
```

Both modes use the same command processing. Blank lines are ignored and processing stops at `EXIT`
(or at the end of the input). Users are stored in `data/users.txt` (relative to the working directory)
and restored at startup.

## Commands

| Command | Description |
|---------|-------------|
| `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta` | Create a user |
| `USER DELETE;Correo;Contraseña` | Delete a user |
| `USER UPDATE;Correo;Contraseña;Campo;NuevoValor` | Update `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` or `CARD` |
| `HELP` | Show the available commands |
| `EXIT` | Terminate the application |

Validation rules: DNI must be 8 digits plus the correct check letter; email must look like
`texto@texto.xxx` (3-character extension); password needs an uppercase letter, a lowercase letter,
a digit and a symbol; card number must be exactly 16 digits. DNI and email must be unique.
