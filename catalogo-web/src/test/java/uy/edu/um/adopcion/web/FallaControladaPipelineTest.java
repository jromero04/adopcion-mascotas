package uy.edu.um.adopcion.web;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Demostracion temporal: esta prueba debe fallar y nunca debe integrarse a main.
 */
class FallaControladaPipelineTest {

    @Test
    void detieneElPipelineAntesDelDespliegue() {
        fail("FALLA CONTROLADA: verificar que Jenkins no ejecute el despliegue");
    }
}
