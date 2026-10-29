# Tasks

## 1. Verification

- [x] 1.1 Run `mvn clean package` and verify `BUILD SUCCESS` and that `target/amusement-park.jar` exists
- [x] 1.2 Verify `unzip -p target/amusement-park.jar META-INF/MANIFEST.MF` contains `Main-Class: es.upm.amusementpark.App`
- [x] 1.3 From a scratch directory, run `printf 'HELP\nEXIT\n' | java -jar <abs>/target/amusement-park.jar` and `java -jar <abs>/target/amusement-park.jar input.txt` (file with `HELP` and `EXIT`), and verify the help text is printed and both runs end normally
