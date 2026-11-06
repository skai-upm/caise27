# Tasks

## 1. Verification

- [x] 1.1 Run `mvn clean package` and verify that it succeeds and that `target/amusement-park.jar` exists
- [x] 1.2 Verify with `unzip -p target/amusement-park.jar META-INF/MANIFEST.MF` that `Main-Class: es.upm.park.App` is declared
- [x] 1.3 Copy the JAR to a scratch directory and verify that `java -jar amusement-park.jar` (interactive, with `HELP`/`EXIT` on stdin) and `java -jar amusement-park.jar input.txt` both run and terminate normally
