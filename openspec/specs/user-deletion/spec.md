# user-deletion Specification

## Purpose

Defines the `USER DELETE` command that removes a user only after the user is identified by email and the password is verified (F1-C05).

## Requirements

### Requirement: USER DELETE command
The system SHALL accept `USER DELETE;Email;Password`. It SHALL locate the user by email, verify that the user exists, check the password, delete the user, and update the persistent information, then report success (for example `USER DELETED`). From Delivery 2, deletion SHALL also be rejected while the user has an active ticket (an individual ticket or an individual ticket within a group ticket dated on or after the current system date) or belongs to a group locked by an active group ticket. When deletion is permitted, the user SHALL be removed from all their groups and attractions, and their historical tickets SHALL be retained.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed and that user exists with that password
- **THEN** the user is removed from memory and from the users file and success is reported

#### Scenario: Unknown email
- **WHEN** `USER DELETE` is processed with an email no user has
- **THEN** no user is removed, the users file is unchanged and an error is reported

#### Scenario: Incorrect password
- **WHEN** `USER DELETE` is processed with an existing email and a wrong password
- **THEN** the user is not deleted, the users file is unchanged and an error is reported

#### Scenario: Malformed delete command
- **WHEN** `USER DELETE;ana@correo.com` is processed
- **THEN** no user is removed and an error is reported

#### Scenario: Active ticket blocks deletion
- **WHEN** Ana has an individual ticket, or an individual ticket within a group ticket, dated today or later and `USER DELETE` is processed for Ana with the correct password
- **THEN** an error is reported and Ana is not deleted

#### Scenario: Locked group blocks deletion
- **WHEN** Ana belongs to a group with an active group ticket and `USER DELETE` is processed for her
- **THEN** an error is reported and Ana is not deleted

#### Scenario: Deletion removes group membership and keeps history
- **WHEN** Ana has only historical tickets and belongs only to unlocked groups and is deleted
- **THEN** Ana is removed from those groups and her historical tickets remain
