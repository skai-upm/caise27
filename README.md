# caise27 — Parque

Command-line application to manage an amusement park: users, groups, tickets and group tickets,
SpeedPasses and Special Passes, typed attractions, and invoices.

## Build

Requires JDK 21+ and Maven.

```bash
mvn package            # compiles, runs the tests and builds target/parque.jar (dependencies included)
```

## Run

| Mode | Command |
|------|---------|
| Interactive (standard input) | `java -jar target/parque.jar` |
| Command file (one command per line) | `java -jar target/parque.jar <fichero>` |
| Temporary persistence (isolated run) | `java -jar target/parque.jar --fly` or `java -jar target/parque.jar --fly <fichero>` |

- Normal runs store data in `data/` (users, groups, tickets, group tickets, passes, attractions).
- `--fly` uses a fresh `tmp/<yyyyMMdd_HHmmss>/` directory that is deleted when the application ends,
  so `data/` is never touched.
- `EXIT` ends the run; blank lines are ignored.

## Commands

Commands use `;` to separate parameters, e.g. `USER CREATE;Ana;Garcia Lopez;12345678Z;ana@correo.com;Clave1!;612345678;1234567890123456`.

- `HELP` lists the syntax of every command.
- `HELP EXTENDED` adds a short description of what each command does.

A complete example covering all deliveries is in
[`src/test/resources/integration/delivery3.txt`](src/test/resources/integration/delivery3.txt)
(expected output: `delivery3.expected`).

## Design

The code is organised in `presentation`, `business` (`model`, `controller`, `security`) and `persistence`
packages. The class model is in [`uml/model.puml`](uml/model.puml); specifications live in `openspec/specs/`.
