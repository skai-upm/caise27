# Spec Delta

## Purpose

Captures the Delivery 3 technical restrictions and the coexistence of all commands from Deliveries 1-3 in every execution mode (source F3-C10, sections 15, 16 and 20).

## ADDED Requirements

### Requirement: Delivery 3 technical restrictions
Delivery 3 SHALL keep the compatible earlier restrictions and use Java 21, Maven and an executable JAR, layered packages, a Builder for invoices, Singleton controllers, injected non-singleton repositories, BCrypt via Maven, bounded generics and interfaces for attractions, non-deleted consumed passes and atomic multi-pass group operations; the only added runtime library SHALL be BCrypt.

#### Scenario: Runtime dependencies
- **WHEN** the `pom.xml` is inspected
- **THEN** the only non-test dependency is `org.mindrot:jbcrypt`, and the executable JAR includes it

### Requirement: All deliveries' commands coexist
All Delivery 1, 2 and 3 commands SHALL work in interactive, file and `--fly` modes from the JAR built by `mvn package`, and `HELP` and `HELP EXTENDED` SHALL include all of them.

#### Scenario: Mixed command file in every mode
- **WHEN** a file mixing commands of all deliveries is run normally and with `--fly`
- **THEN** valid commands succeed, invalid ones report errors, and the `--fly` run leaves no data behind
