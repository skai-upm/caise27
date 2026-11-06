# Proposal

## Why

Milestone F3-C10 (Delivery 3, step 10) closes Delivery 3: commands from all three deliveries must coexist, `HELP` must list every syntax — including each new Delivery 3 variant — and `HELP EXTENDED` must explain every command, while the Delivery 3 technical restrictions hold and the JAR runs a command file both normally and with `--fly` leaving no temporary data.

## What Changes

- `HELP` lists the `ATTRACTION CREATE` and `ATTRACTION ENTER` variants as separate syntaxes (with and without `<type>`, with and without `SPEED`), as the Delivery 3 command list states them; `HELP EXTENDED` explains each of them.
- Verify the complete Delivery 3 flow end-to-end with a command file (typed attractions, passes, SPEED and special entry, invoices, refunds, extended help) in normal and `--fly` mode, with identical output and no leftover temporary data, while the Delivery 1 and Delivery 2 flows keep working.
- Verify the Delivery 3 technical restrictions as a whole (layers, Builder, singletons, injected repositories, BCrypt as the only runtime dependency, bounded generics, consumed passes kept, atomic group SpeedPass entry) and document Delivery 3 in the README.

### In scope
- Source sections 15, 16 and 20.

### Out of scope
- New features. Git-history requirements (section 18) are met by the project history.

## Capabilities

### New Capabilities
- `delivery-3-integration`: end-to-end Delivery 3 behavior and its technical restrictions verified as a whole.

### Modified Capabilities
- `execution-control`: `HELP` lists the attraction creation and entry variants separately.

## Impact

- `CommandCatalog` entries; tests asserting help text; new end-to-end `MainTest`; README.
