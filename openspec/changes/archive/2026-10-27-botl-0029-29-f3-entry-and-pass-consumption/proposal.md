# Proposal

## Why

Milestone F3-C08 extends `ATTRACTION ENTER` so that passes are actually used: an optional `SPEED` variant consumes one SpeedPass per individual ticket (atomically for group tickets), and entering a SPECIAL attraction automatically consumes the ticket's Special Pass — always only when entry succeeds.

## What Changes

- `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` in addition to the Delivery 2 syntax.
- Individual ticket with SPEED (GENERAL/INDIVIDUAL attractions): requires an unconsumed SpeedPass; on success exactly one is consumed and reported.
- Group ticket with SPEED (GENERAL/GROUP attractions): every composed ticket needs an unconsumed SpeedPass; date, type, capacity and passes are all checked before any change; on success all members enter and one SpeedPass per composed ticket is consumed — otherwise nobody enters and nothing is consumed.
- SPECIAL attractions: normal syntax only; the Special Pass is found automatically and consumed on successful entry (reported); SPEED and group tickets rejected.
- Failed entries never modify passes.
- HELP lists the SPEED variant.
- Record the mandatory milestone commit `[F3-C08] Extend attraction access and pass consumption` once verified.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `attraction-management`: SpeedPass entry, Special Pass consumption, atomic group entry with SpeedPasses.
- `command-processing`: HELP lists `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`.

## Impact

- `AccessTicket.getIndividualTickets()`, `Attraction` (speed acceptance, post-admission hook), `SpecialAttraction`, new `business.service.EntryResult`, `AttractionService.enter(…, speed)`, `CommandProcessor`, `CommandCatalog`, tests, UML.
