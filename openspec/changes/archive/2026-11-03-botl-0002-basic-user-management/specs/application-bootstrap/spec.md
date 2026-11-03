# Spec Delta

## REMOVED Requirements

### Requirement: No domain functionality
**Reason**: The basic user-management epic intentionally introduces the first domain functionality (users), so the bootstrap-stage restriction no longer applies.
**Migration**: Domain behavior is now governed by the `user-model`, `user-validation`, `user-management` and `user-file-persistence` capabilities.
