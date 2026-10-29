package es.upm.amusementpark.business.invoice;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.User;

import java.util.ArrayList;
import java.util.List;

/** Decides which construction steps apply to each kind of invoice. */
public class InvoiceDirector {

    /**
     * @param directTickets individual tickets bought directly for the user
     * @param passes passes associated with any of the user's individual tickets
     */
    public Invoice userInvoice(User user, List<IndividualTicket> directTickets, List<Pass> passes) {
        List<SpeedPass> speedPasses = new ArrayList<>();
        List<SpecialPass> specialPasses = new ArrayList<>();
        for (Pass pass : passes) {
            if (pass instanceof SpeedPass speed) {
                speedPasses.add(speed);
            } else if (pass instanceof SpecialPass special) {
                specialPasses.add(special);
            }
        }
        return new UserInvoiceBuilder().addUser(user).addIndividualTickets(directTickets)
                .addSpeedPasses(speedPasses).addSpecialPasses(specialPasses).build();
    }

    public Invoice groupInvoice(Group group, List<GroupTicket> groupTickets) {
        return new GroupInvoiceBuilder().addGroup(group).addGroupTickets(groupTickets).build();
    }
}
