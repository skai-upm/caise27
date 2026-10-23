package es.upm.amusementpark.persistence;

import java.io.IOException;

/**
 * A piece of persistence work that may fail with an {@link IOException}.
 */
@FunctionalInterface
public interface IoAction {

    void run() throws IOException;
}
