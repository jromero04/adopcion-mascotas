package uy.edu.um.adopcion.registro.servicio;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uy.edu.um.adopcion.registro.modelo.Especie;
import uy.edu.um.adopcion.registro.modelo.Mascota;
import uy.edu.um.adopcion.registro.modelo.Sexo;
import uy.edu.um.adopcion.registro.repositorio.MascotaRepository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegistroMascotaServiceTest {

    @Mock
    private MascotaRepository mascotaRepository;

    private RegistroMascotaService registroMascotaService;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        registroMascotaService = new RegistroMascotaService(mascotaRepository, validator);
        lenient().when(mascotaRepository.save(any(Mascota.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    @Test
    void registraDatosValidos() {
        Mascota mascota = mascotaValida();

        assertDoesNotThrow(() -> registroMascotaService.registrar(mascota));

        verify(mascotaRepository).save(mascota);
    }

    @Test
    void permiteNombreAusente() {
        Mascota mascota = mascotaValida();
        mascota.setNombre("   ");

        Mascota guardada = registroMascotaService.registrar(mascota);

        assertNull(guardada.getNombre());
    }

    @Test
    void permiteEdadYSexoDesconocidos() {
        Mascota mascota = new Mascota(Especie.GATO, null, null, "https://ejemplo.test/gato");

        Mascota guardada = registroMascotaService.registrar(mascota);

        assertNull(guardada.getEdadMeses());
        assertEquals(Sexo.DESCONOCIDO, guardada.getSexo());
    }

    @Test
    void rechazaEdadNegativa() {
        Mascota mascota = mascotaValida();
        mascota.setEdadMeses(-1);

        assertInvalida(mascota);
    }

    @Test
    void rechazaEspecieAusente() {
        Mascota mascota = mascotaValida();
        mascota.setEspecie(null);

        assertInvalida(mascota);
    }

    @Test
    void rechazaUrlDeFotoAusente() {
        Mascota mascota = mascotaValida();
        mascota.setFotoUrl("   ");

        assertInvalida(mascota);
    }

    @Test
    void rechazaUrlDeFotoConEsquemaInvalido() {
        Mascota mascota = mascotaValida();
        mascota.setFotoUrl("ftp://ejemplo.test/perro");

        assertInvalida(mascota);
    }

    private void assertInvalida(Mascota mascota) {
        assertThrows(ConstraintViolationException.class,
                () -> registroMascotaService.registrar(mascota));
        verify(mascotaRepository, never()).save(mascota);
    }

    private Mascota mascotaValida() {
        Mascota mascota = new Mascota(
                Especie.PERRO,
                Sexo.HEMBRA,
                18,
                "https://ejemplo.test/perro"
        );
        mascota.setNombre("Luna");
        mascota.setDescripcion("Encontrada cerca de la plaza");
        return mascota;
    }
}
