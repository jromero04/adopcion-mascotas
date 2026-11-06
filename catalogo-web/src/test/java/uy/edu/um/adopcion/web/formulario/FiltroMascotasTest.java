package uy.edu.um.adopcion.web.formulario;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FiltroMascotasTest {

    private static Validator validator;

    @BeforeAll
    static void crearValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void rechazaEdadMinimaNegativa() {
        FiltroMascotas filtro = new FiltroMascotas();
        filtro.setEdadMinima(-1);

        assertFalse(validator.validate(filtro).isEmpty());
    }

    @Test
    void rechazaEdadMaximaNegativa() {
        FiltroMascotas filtro = new FiltroMascotas();
        filtro.setEdadMaxima(-1);

        assertFalse(validator.validate(filtro).isEmpty());
    }

    @Test
    void rechazaMinimoMayorQueMaximo() {
        FiltroMascotas filtro = new FiltroMascotas();
        filtro.setEdadMinima(13);
        filtro.setEdadMaxima(12);

        assertFalse(validator.validate(filtro).isEmpty());
    }

    @Test
    void permiteUnSoloLimite() {
        FiltroMascotas filtro = new FiltroMascotas();
        filtro.setEdadMinima(12);

        assertTrue(validator.validate(filtro).isEmpty());
    }
}
