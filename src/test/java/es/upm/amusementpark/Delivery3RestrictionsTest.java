package es.upm.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.amusementpark.business.GeneralAccess;
import es.upm.amusementpark.business.GeneralAttraction;
import es.upm.amusementpark.business.GroupAccess;
import es.upm.amusementpark.business.GroupAttraction;
import es.upm.amusementpark.business.IndividualAccess;
import es.upm.amusementpark.business.IndividualAttraction;
import es.upm.amusementpark.business.ParkController;
import es.upm.amusementpark.business.SpecialAttraction;
import es.upm.amusementpark.business.invoice.GroupInvoiceBuilder;
import es.upm.amusementpark.business.invoice.InvoiceBuilder;
import es.upm.amusementpark.business.invoice.UserInvoiceBuilder;
import es.upm.amusementpark.persistence.ParkStorage;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import org.junit.jupiter.api.Test;

class Delivery3RestrictionsTest {

    @Test
    void invoicesUseBuilderHierarchy() {
        assertEquals(InvoiceBuilder.class, UserInvoiceBuilder.class.getSuperclass());
        assertEquals(InvoiceBuilder.class, GroupInvoiceBuilder.class.getSuperclass());
    }

    @Test
    void controllerSingletonRepositoriesNot() {
        assertEquals(0, ParkController.class.getConstructors().length);
        assertTrue(ParkStorage.class.getConstructors().length > 0);
    }

    @Test
    void attractionsUseBoundedGenerics() {
        TypeVariable<?> parameter = es.upm.amusementpark.business.Attraction.class.getTypeParameters()[0];
        assertEquals(GeneralAccess.class, parameter.getBounds()[0]);
        Map<Class<?>, Class<?>> expected = Map.of(GeneralAttraction.class, GeneralAccess.class,
                IndividualAttraction.class, IndividualAccess.class, GroupAttraction.class, GroupAccess.class,
                SpecialAttraction.class, IndividualAccess.class);
        expected.forEach((type, bound) -> assertEquals(bound,
                ((ParameterizedType) type.getGenericSuperclass()).getActualTypeArguments()[0]));
    }
}
