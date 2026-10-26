# Proposal

## Why

Every attraction currently admits any ticket. Delivery 3 specialises attractions by the kind of ticket they accept — general, individual-only, group-only and special — and asks for a design where interfaces represent ticket access capabilities and bounded generic types restrict, in a functional way, which ticket classes each attraction can handle.

## What Changes

- Four attraction types:
  - `GENERAL` — admits individual and group tickets (the Delivery 2 behaviour);
  - `INDIVIDUAL` — admits only individual tickets;
  - `GROUP` — admits only group tickets;
  - `SPECIAL` — admits only individual tickets, as the basis on which its Special Pass rules are applied (pass consumption arrives with the next change).
- `ATTRACTION CREATE;<id>;<name>;<capacity>` keeps working and creates a `GENERAL` attraction; the new form `ATTRACTION CREATE;<id>;<name>;<capacity>;<type>` sets the type explicitly.
- Ticket access capabilities are represented by interfaces (admission by a single user's ticket, admission by a group's ticket) implemented by the ticket classes; attractions are a generic class bounded by those interfaces, and each type fixes the accepted capability, which is checked and used when admitting.
- Entering with a ticket kind the attraction does not accept is rejected with an `ERROR` line and nobody enters.
- `ATTRACTION STATUS` also shows the type; the type is stored with the attraction and restored at startup.
- HELP lists both creation forms.

## Capabilities

### New Capabilities
<!-- None -->

### Modified Capabilities
- `attraction-management`: attractions have a type that restricts the accepted ticket kinds; creation accepts an optional type (default `GENERAL`); entry checks the type; status and persistence include the type; access capabilities are modelled with interfaces and bounded generics.

## Impact

- New model interfaces `Admission`, `IndividualAdmission`, `GroupAdmission` (implemented by `Ticket`, `IndividualTicket`, `GroupTicket`); `Attraction<T extends Admission>` becomes an abstract generic base with `GeneralAttraction`, `IndividualAttraction`, `GroupAttraction`, `SpecialAttraction`; new `AttractionType` enum.
- `AttractionController`, `AttractionRepository` (type field), `CommandProcessor` (optional parameter, status line, registry supports two syntaxes of one command).
- `uml/model.puml` updated. Tests for every type/ticket combination, defaults, persistence and output.
