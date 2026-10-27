# Spec Delta

## ADDED Requirements

### Requirement: Passes on user invoices
A user invoice SHALL include, in separate sections, the SpeedPasses and the Special Passes associated with the user's individual tickets (including tickets that are part of a group ticket) with their prices; sections without passes are omitted, and group invoices SHALL NOT add passes to their total.

#### Scenario: Passes on a group component
- **WHEN** Ana's only purchases are a SpeedPass (7.20) on `TG01:12345678Z`
- **THEN** her invoice has a SpeedPasses section with that pass, no individual-tickets section and total 7.20, and `G01`'s invoice total is unchanged
