# caise27

Amusement Park application (Java, Maven).

## Build prerequisites

- JDK 21 (the compiler `release` is pinned to 21 in `pom.xml`)
- Apache Maven 3.9+

## Build

```bash
mvn compile   # compile sources
mvn test      # run unit tests
mvn package   # build the executable JAR target/amusement-park.jar
```

## Run

```bash
java -jar target/amusement-park.jar            # interactive mode
java -jar target/amusement-park.jar input.txt  # process a commands file
```
