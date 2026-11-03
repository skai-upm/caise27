## REMOVED Requirements

### Requirement: No domain functionality in the bootstrap
**Reason**: This constraint applied only to the bootstrap delivery; this change intentionally adds the first domain functionality (user management).
**Migration**: None needed. Domain behaviour is now specified by `user-model`, `user-validation`, `user-management` and `user-file-persistence`.
