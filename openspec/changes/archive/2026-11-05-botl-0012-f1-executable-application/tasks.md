# Tasks

## 1. Guards

- [x] 1.1 Add `ExecutableJarTest` checking the `pom.xml` manifest `mainClass` resolves to a class with `public static void main(String[])`, and that interactive input keeps being processed until `EXIT`; verify it passes

## 2. Verification

- [x] 2.1 Run `mvn -q package` and verify `target/amusement-park.jar` has `Main-Class: es.upm.amusementpark.App`, starts with `java -jar` waiting for stdin commands, and processes a command file argument
