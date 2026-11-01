package es.upm.amusementpark.business.model;

/**
 * Access capability of a whole group, composed of one individual admission per member.
 */
public interface GroupAdmission extends Admission {

    Group getGroup();
}
