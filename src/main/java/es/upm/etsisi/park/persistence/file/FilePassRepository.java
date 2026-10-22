package es.upm.etsisi.park.persistence.file;

import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.pass.Pass;
import es.upm.etsisi.park.persistence.PassRepository;
import es.upm.etsisi.park.persistence.TicketRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Passes stored in a text file through a {@link PassFileStore}.
 */
public class FilePassRepository extends ListRepository<Pass> implements PassRepository {

    private final PassFileStore store;

    public FilePassRepository(PassFileStore store, TicketRepository tickets) {
        super(store.load(tickets::findById), Pass::getId);
        this.store = store;
    }

    @Override
    protected void persist(List<Pass> current) {
        store.save(current);
    }

    @Override
    public List<Pass> findByTicket(IndividualTicket ticket) {
        List<Pass> result = new ArrayList<>();
        for (Pass pass : items) {
            if (pass.getTicket() == ticket) {
                result.add(pass);
            }
        }
        return result;
    }

    @Override
    public void addAll(Collection<? extends Pass> passes) {
        items.addAll(passes);
        save();
    }

    @Override
    public void removeAll(Collection<? extends Pass> passes) {
        items.removeAll(passes);
        save();
    }
}
