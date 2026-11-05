# Design

## Context

`pom.xml` already sets `finalName` `amusement-park` and the `maven-jar-plugin` manifest `mainClass` `es.upm.amusementpark.App` (Delivery 1 integration change). See proposal.md - Why.

## Decisions

- **Reuse the `application-runtime` capability** instead of creating `executable-application`, which would duplicate the existing requirement.
- **Test the configuration, not the built artifact, inside `mvn test`**: unit tests run before `package`, so they read `<mainClass>` from `pom.xml`, load that class reflectively and check its `main` signature. The built JAR is verified by running `mvn package` and `java -jar` in the task list. A Failsafe integration test was rejected because it adds a plugin and lifecycle phase the milestone doesn't need.
