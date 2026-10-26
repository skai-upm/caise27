package es.upm.parque;

import java.time.LocalDate;

/**
 * The application's single current date. It starts as the operating-system date and can then be changed
 * freely with SYSTEM DATE; every time-dependent rule must read it from here.
 */
public class SystemDate {
    private LocalDate date = LocalDate.now();

    public LocalDate get() {
        return date;
    }

    public void set(LocalDate date) {
        this.date = date;
    }
}
