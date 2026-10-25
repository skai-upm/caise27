# caise27

Amusement Park terminal application (Java 21, Maven).

## Build

```bash
mvn test       # compile and run the tests
mvn package    # build the executable JAR target/parque.jar
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

Users are stored in `data/users.txt`, relative to the working directory.

## Commands

| Command | Description |
|---|---|
| `USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta` | Register a user |
| `USER DELETE;Correo;Contraseña` | Delete a user |
| `USER UPDATE;Correo;Contraseña;Campo;NuevoValor` | Update `NAME`, `SURNAME`, `EMAIL`, `PASSWORD`, `PHONE` or `CARD` |
| `HELP` | List the available commands |
| `EXIT` | Terminate the application |
