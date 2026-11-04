# Proposal

## Why

Passes can be bought and refunded but not used. Attraction entry must now consume SpeedPasses on request and Special Passes at SPECIAL attractions, without ever consuming passes or partially admitting groups when an entry fails.

## What Changes

- `ATTRACTION ENTER;IdAtraccion;IdTicket;SPEED` requests SpeedPass use; the two-parameter form is unchanged.
- Individual SPEED entry (GENERAL, INDIVIDUAL) consumes exactly one unused SpeedPass of the ticket.
- Group SPEED entry (GENERAL, GROUP) requires one unused SpeedPass per member ticket and consumes one each, all-or-nothing.
- SPECIAL attractions require and automatically consume the ticket's unused Special Pass; `SPEED` is rejected there.
- Every entry is fully validated before any mutation; consumed passes are persisted and named in the output.
- `HELP` and UML updated.

## Capabilities

### New Capabilities
- `pass-based-access`: entry command forms, SpeedPass and Special Pass consumption rules, atomicity.

### Modified Capabilities
- `application-control`: `HELP` lists `ATTRACTION ENTER;IdAtraccion;IdTicket;SPEED`.

## Impact

- `Attraction` gains a non-mutating `checkAdmission`; `SpecialAttraction` marks that it requires a Special Pass; `PassManager` gains lookups of unused passes and an atomic `consume`; `ParkController.enterAttraction` orchestrates preflight → admit → consume; `CommandProcessor`/`CommandCatalog` handle the option.
- Delivery milestone message: `[F3-C08] Extend attraction access and pass consumption`.
