# caise27

## Amusement Park (Delivery 1)

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
```

Both modes use the same command processing. Blank lines in files are ignored and `EXIT` stops
processing immediately. Users are stored in `users.txt` (one user per line, fields separated by `;`)
in the working directory and reloaded at startup.

### Commands

```text
USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta
USER DELETE;Correo;Contraseña
USER UPDATE;Correo;Contraseña;Campo;NuevoValor   (Campo: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)
HELP
EXIT
```
