# Tasks

## 1. Model regeneration

- [x] 1.1 Rewrite `uml/model.puml` from the current source: classes, abstract ticket hierarchy, enumeration, value-type attributes without technical labels or methods, named directed relationships with crosses and multiplicities, aggregation/composition, and usage arrows only for classes without incoming relationships; verify PlantUML `-checkonly` succeeds
- [x] 1.2 Cross-check every attribute and relationship in the model against the fields in `src/main/java` (scripted comparison of association labels vs. field names); verify no missing or invented element
- [x] 1.3 Render the model (Smetana layout) to inspect it, run `mvn test` and verify `git diff --stat -- src` is empty
