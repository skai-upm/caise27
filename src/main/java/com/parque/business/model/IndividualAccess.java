package com.parque.business.model;

/**
 * Access capability of an individual ticket: it admits exactly its owner.
 */
public interface IndividualAccess extends Admission {
    User getUser();
}
