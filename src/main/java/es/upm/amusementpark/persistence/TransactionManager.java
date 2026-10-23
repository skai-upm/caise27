package es.upm.amusementpark.persistence;

import java.io.IOException;

/**
 * Groups several repository saves so that they are written all together or
 * not at all.
 */
public interface TransactionManager {

    void inTransaction(IoAction action) throws IOException;
}
