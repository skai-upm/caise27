package es.upm.park.business.model;

/** Park information that attraction-specific entry rules may need. */
@FunctionalInterface
public interface EntryRules {

    boolean hasUnconsumedSpecialPass(IndividualAccess ticket);
}
