package es.upm.amusementpark.business.model;

/** Query about the passes of a ticket, provided by central management (tickets do not know their passes). */
@FunctionalInterface
public interface PassLookup {

    boolean hasUnconsumedSpecialPass(IndividualAccess ticket);
}
