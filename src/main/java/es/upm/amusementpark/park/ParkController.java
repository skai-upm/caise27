package es.upm.amusementpark.park;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import es.upm.amusementpark.model.User;
import es.upm.amusementpark.persistence.UserStorage;
import es.upm.amusementpark.repository.UserRepository;
import es.upm.amusementpark.service.UserService;

/**
 * Single central manager of the park state during an execution: users, current system date and
 * current base ticket price.
 *
 * <p>Only one instance exists; it is obtained with {@link #getInstance()}.
 */
public final class ParkController {

    public static final BigDecimal INITIAL_BASE_PRICE = new BigDecimal("40.00");

    private static final ParkController INSTANCE = new ParkController();

    private UserService users;
    private LocalDate currentDate;
    private BigDecimal basePrice;

    private ParkController() {
        reset(new UserRepository(), null);
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /**
     * Prepares the park state for a new execution: loads the stored users and sets the date to
     * today and the base price to its initial value.
     *
     * @return warnings produced while loading stored data
     */
    public List<String> initialize(UserStorage storage) throws IOException {
        List<String> warnings = new ArrayList<>();
        UserRepository repository = new UserRepository();
        for (User user : storage.load(warnings)) {
            repository.save(user);
        }
        reset(repository, storage);
        return warnings;
    }

    private void reset(UserRepository repository, UserStorage storage) {
        this.users = new UserService(repository, storage);
        this.currentDate = LocalDate.now();
        this.basePrice = INITIAL_BASE_PRICE;
    }

    public UserService users() {
        return users;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate currentDate) {
        this.currentDate = currentDate;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }
}
