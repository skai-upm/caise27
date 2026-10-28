# Spec Delta

## ADDED Requirements

### Requirement: Passes on user invoices
A user invoice SHALL include a SpeedPasses section and a Special Passes section listing the passes associated with the user's individual tickets — including tickets that belong to a group ticket composition — each with its price, and SHALL add them to the total. Group invoices SHALL NOT include members' passes. Sections without passes SHALL be omitted.

#### Scenario: Passes on a group composition ticket
- **WHEN** Ana's only ticket is `TG01:12345678Z` (36.00) and she bought SpeedPass `SP01:1` (7.20) for it
- **THEN** her invoice shows no ticket section, a SpeedPasses section with `SP01:1` at 7.20 and a total of 7.20, and the group invoice total does not include it
