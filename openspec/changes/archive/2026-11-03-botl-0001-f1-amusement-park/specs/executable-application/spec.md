# Spec Delta

## Purpose

Ensures the application can be built with `mvn package` and run from a terminal with `java -jar`, independently of any IDE (source F0-C02, sections 2.2 and 17.2).

## ADDED Requirements

### Requirement: Executable JAR is produced by Maven
Running `mvn package` SHALL produce an executable JAR under `target/` whose manifest declares the application entry point, so that it starts with `java -jar` without an IDE, additional configuration, or classpath arguments.

#### Scenario: JAR built and started
- **WHEN** `mvn package` completes successfully and the produced JAR is run with `java -jar` and no arguments
- **THEN** the application starts and waits for commands on standard input

### Requirement: JAR accepts a command file argument
When started with `java -jar <jar> <file>`, the application SHALL process the commands contained in the given text file using the same command processing used for standard input.

#### Scenario: File argument is processed
- **WHEN** the JAR is run with the path of a text file containing valid commands as its first argument
- **THEN** the commands in the file are executed and their results are reported
