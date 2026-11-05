# Tasks

## 1. Composition

- [x] 1.1 Add `Repositories` (files/in-memory factories) and change `Park.start` to receive it; verify tests for both factories
- [x] 1.2 Make the five controllers Singletons with `inject(...)`, make `CommandProcessor` take them by constructor, and add `ApplicationInitializer`; wire `App`; verify the singleton and injection tests (same instance, no public constructor, injected repositories used, two directory configurations)

## 2. Regression

- [x] 2.1 Rewire existing tests through the initializer; run `mvn clean package` and the JAR in normal and `--fly` modes and verify all tests pass
