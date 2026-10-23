package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.exception.InvalidDataException;
import es.upm.amusementpark.business.exception.PersistenceException;
import es.upm.amusementpark.persistence.IoAction;
import es.upm.amusementpark.persistence.TransactionManager;
import java.io.IOException;

/**
 * Behavior shared by the controllers: saving through the injected
 * {@link TransactionManager} and restoring the in-memory state when saving
 * fails.
 */
abstract class AbstractController {

    private TransactionManager transactions;

    protected void setTransactions(TransactionManager transactions) {
        this.transactions = transactions;
    }

    /**
     * Runs {@code save} in one transaction. If it fails, {@code undo} restores
     * the in-memory state as it was before the command.
     */
    protected void persist(IoAction save, Runnable undo) throws PersistenceException {
        try {
            transactions.inTransaction(save);
        } catch (IOException e) {
            undo.run();
            throw new PersistenceException("Could not save park data: " + e.getMessage(), e);
        }
    }

    protected static void requireText(String value, String label) throws InvalidDataException {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidDataException(label + " must not be empty");
        }
    }
}
