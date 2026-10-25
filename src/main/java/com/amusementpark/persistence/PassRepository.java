package com.amusementpark.persistence;

import com.amusementpark.business.model.IndividualTicket;

import java.io.IOException;
import java.util.List;

/**
 * Storage of the personal passes of individual tickets.
 */
public interface PassRepository {

    /**
     * Reads the stored passes and attaches each one to its ticket among {@code tickets}.
     */
    void load(List<IndividualTicket> tickets) throws IOException;

    /**
     * Replaces the stored passes with those currently attached to {@code tickets}.
     */
    void save(List<IndividualTicket> tickets) throws IOException;
}
