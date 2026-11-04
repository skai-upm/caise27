# Spec Delta

## REMOVED Requirements

### Requirement: Tickets with passes are not deleted alone
**Reason**: It was an interim guard against orphan passes until cascade deletion was specified; Delivery 3 now requires valid ticket deletions to cascade to their passes.
**Migration**: See the `ticket-cascade-deletion` capability: `TICKET DELETE` and `GROUPTICKET DELETE` now remove the associated passes instead of being rejected.
