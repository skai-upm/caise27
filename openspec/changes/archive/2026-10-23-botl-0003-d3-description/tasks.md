# Tasks

## 1. Layered reorganisation, exceptions and controllers

- [x] 1.1 Move classes into `presentation`, `business` (model, exception, controller) and `persistence` packages; introduce the `ParkException` hierarchy replacing `UserException`; verify `mvn test` passes with tests moved accordingly
- [x] 1.2 Split `Park`/`UserService` into singleton controllers (`SystemController`, `UserController`, `GroupController`, `TicketController`, `AttractionController`) configured by `ApplicationContext` with injected repository interfaces and collaborating controllers; replace `ParkStorage` with `TextRepositories`; verify all Delivery 1–2 behaviour tests pass against the controllers and that each controller is a single instance

## 2. Presentation and help

- [x] 2.1 Replace the `CommandProcessor` switch with a `CommandRegistry` filled by command groups, keeping all outputs; verify the existing command tests pass
- [x] 2.2 Add `HelpGenerator` with injected `HelpRenderer` variants (`HELP`, `HELP EXTENDED`) built from the registry; verify tests that HELP lists every syntax in the command-interface spec and HELP EXTENDED adds a description per command

## 3. Password protection

- [x] 3.1 Add the jBCrypt dependency, `PasswordHasher`/`BCryptPasswordHasher`, store only hashes and verify with BCrypt in `UserController`; verify tests that `users.txt` holds `$2a$` hashes, authentication and password change work, and plain-text values fail

## 4. Passes

- [x] 4.1 Add `Pass`, `SpeedPass`, `SpecialPass` linked to `IndividualTicket` and `PassController` (purchase 1/10 with atomic id checks, Special Pass uniqueness, prices, refunds) with SPEEDPASS/SPECIALPASS commands; verify with unit tests for every passes scenario
- [x] 4.2 Cascade pass deletion from `TICKET DELETE` and `GROUPTICKET DELETE`; verify with unit tests for the modified ticket deletion scenarios

## 5. Attraction types and access

- [x] 5.1 Add `Access`/`IndividualAccess`/`GroupAccess` implemented by the tickets, generic `Attraction<T extends Access>` with `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, `SpecialAttraction` and `AttractionType`, plus the optional type on ATTRACTION CREATE; verify with unit tests for type acceptance and creation scenarios
- [x] 5.2 Extend ATTRACTION ENTER with `SPEED` and automatic Special Pass consumption, atomic for groups, printing consumed passes; verify with unit tests for each entry scenario including no pass change on failure

## 6. Invoices

- [x] 6.1 Add `Invoice`, `InvoiceBuilder`, `UserInvoiceBuilder`, `GroupInvoiceBuilder`, `InvoiceController` (director) and INVOICE CREATE; verify with unit tests for the user (167.20 example) and group invoices, omitted sections and error cases

## 7. Persistence, build and documentation

- [x] 7.1 Add `PassRepository`/`TextPassRepository` and the attraction type column (legacy 3-column lines as GENERAL); verify with unit tests for pass round-trip with consumed state, type restore, legacy lines and `--fly` coverage
- [x] 7.2 Configure `maven-shade-plugin` so `target/amusement-park.jar` bundles jBCrypt; verify the JAR runs from another directory and creates a user
- [x] 7.3 Update `uml/model.puml` (layers, singleton controllers, DI relations, access interfaces, generic attractions, passes with cardinalities, builder classes) and `README.md`; verify the diagram passes PlantUML syntax checking and lists every class it shows from the code

## 8. Integration

- [x] 8.1 Run `mvn clean package` and the assignment's Delivery 3 example through the JAR in normal and `--fly` modes, checking pass prices, consumption messages, both invoices, HELP/HELP EXTENDED, data files after a normal run and no temporary files after `--fly`
