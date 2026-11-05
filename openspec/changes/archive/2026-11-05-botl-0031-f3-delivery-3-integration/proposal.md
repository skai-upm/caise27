## Why

Delivery 3, milestone F3-C10 (source sections 15, 16, 18 and 20), requires all features of the three deliveries to work together. It covers four things:
- **HELP** lists the Delivery 3 syntaxes explicitly, alongside the Delivery 1 and 2 ones, and **HELP EXTENDED** describes each command.
- **Technical restrictions** are all preserved.
- **The JAR** runs with an external command file, in both normal and `--fly` modes.
- **The Git history** contains the mandated F3 milestone messages.

## What Changes

- **HELP catalog consolidation**, the same consolidation as F2's integration milestone. The bracketed forms are split into the literal syntaxes that source section 15 requires:
  - `ATTRACTION CREATE;<identifier>;<name>;<capacity>` and `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>`;
  - `ATTRACTION ENTER;<attraction>;<ticket>` and `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`.
- **Pass command order.** The pass commands follow the section 15 order (`SPEEDPASS CREATE`, `SPEEDPASS DELETE`, `SPECIALPASS CREATE`, `SPECIALPASS DELETE`) and come after the attraction commands, then the invoices, `HELP`, `HELP EXTENDED` and `EXIT`. The full HELP list has 29 syntaxes, each with a purpose in HELP EXTENDED.
- **New `Delivery3IntegrationTest`.** It runs a combined Delivery 1+2+3 command file through the real entry point (`App.execute`):
  - in normal mode with a restart (state, BCrypt passwords, types and consumed passes restored);
  - in `--fly` mode, with the same responses, nothing left behind and the normal files untouched.
- **New `delivery-integration` capability**, which records the three-delivery compatibility and the technical restrictions.
- **README.** An integration section listing the Delivery 3 run commands.
- **Git history.** Each F3 milestone message (`[F3-C01]` … `[F3-C09]`, and `[F3-C10]` here) is recorded, in order, in the commit bodies of the BOTL commits. The commit subjects follow the BOTL subject format required by the batch. This is a known divergence from "commits with exactly the specified text" and is reported, not hidden.

## Capabilities

### New Capabilities
- `delivery-integration`: coexistence of the three deliveries and preservation of the technical restrictions.

### Modified Capabilities
- `execution-control`: HELP lists the explicit Delivery 3 syntaxes in the consolidated order.

## Impact

- `presentation.CommandCatalog`: entries split and reordered; the purpose text is unchanged in substance.
- Tests: `CommandProcessorTest` (catalog size), the new `Delivery3IntegrationTest`, and `HelpTest`, which still passes because the catalog is the source of truth.
- README.
- No business, persistence or model change.
