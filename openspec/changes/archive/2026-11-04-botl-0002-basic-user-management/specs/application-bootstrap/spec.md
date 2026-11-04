# Spec Delta

## REMOVED Requirements

### Requirement: No domain functionality in the bootstrap
**Reason**: The bootstrap phase is over; basic user management (change botl-0002) intentionally adds amusement-park domain functionality.
**Migration**: None needed. Domain behavior is now specified by the `user-model`, `user-validation`, `user-management` and `user-file-persistence` capabilities.
