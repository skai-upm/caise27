# Tasks

## 1. Invoices with passes

- [x] 1.1 Add SpeedPass and Special Pass steps to `UserInvoiceBuilder`, use them from `InvoiceDirector`/`InvoiceController` for all the user's individual tickets; verify with a test reproducing the 166.40 scenario, empty pass sections omitted, and group invoices unchanged

## 2. Integration

- [x] 2.1 Add `Delivery3IntegrationTest`: every Delivery 3 command with earlier commands in one flow, Delivery 3 HELP minimum set in HELP and HELP EXTENDED with descriptions; verify it passes
- [x] 2.2 Run `mvn clean package` and verify `BUILD SUCCESS`; run `java -jar <abs>/target/amusement-park.jar input_fase3.txt` and `--fly input_fase3.txt` from a scratch directory and verify controlled output and no data left by `--fly`
