# Design

## Context

All Delivery 3 milestones F3-C01…F3-C09 are implemented with unit, command and integration tests; HELP comes from `CommandCatalog`.

## Decisions

- **`Delivery3IntegrationTest`** via `App.run` in a temporary working directory, seeding `data/users.txt` (users cannot be created by command yet in this batch order); asserts outputs line by line where order matters and counts errors; restarts to check persisted state; runs a user-free file in normal and `--fly` mode and compares.
- **`Delivery3HelpTest`**: the Delivery 3 minimum lines (source §15) are contained in `HELP`; `HELP EXTENDED` shows a description for every catalogue entry (already covered generally; this pins the delivery list).
- **`Delivery3RestrictionsTest`**: reflection/source checks gathered in one place (layer packages; `InvoiceBuilder` abstract with two concrete builders and a director; private constructors and static instance of both controllers; `TextRepositories.inDirectory` returns new instances; `org.mindrot:jbcrypt` declared; attraction subclasses bound to access interfaces; consumed passes remain after entry). Existing focused tests remain the primary evidence.
