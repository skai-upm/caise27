# Design

## Decisions

- **`UserRepository(Path)`** follows the other repositories: `load(park, warnings)` and `save(park)` use `TextFile`. Fields are joined and split with `;`, which never appears inside fields because commands are split on `;` and BCrypt output uses `[./A-Za-z0-9$]` only.
- **Constructor injection**: `ParkStorage(dir, users, attractions, groups, tickets, passes)`. `inDirectory` configures `users.txt`. Users load first because later elements may refer to them.
- **`restoreUser`** creates the `User` with the stored password as is. It checks DNI and e-mail uniqueness (throwing `ParkException`) but doesn't re-apply format validation, so historical data stays readable even if rules change.
