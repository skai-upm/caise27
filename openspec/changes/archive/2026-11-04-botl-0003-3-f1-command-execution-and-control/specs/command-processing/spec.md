# Spec Delta

## REMOVED Requirements

### Requirement: Terminal command input
**Reason**: Reading commands only from standard input until end of input is superseded by the interactive and file input modes, which also stop on `EXIT`.
**Migration**: See `command-input-modes` ("Interactive mode", "File mode", "Shared command processing").
