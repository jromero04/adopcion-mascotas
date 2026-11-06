package uy.edu.um.adopcion.web.servicio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import uy.edu.um.adopcion.registro.modelo.Especie;
import uy.edu.um.adopcion.registro.modelo.Mascota;
import uy.edu.um.adopcion.registro.modelo.Sexo;
import uy.edu.um.adopcion.registro.repositorio.MascotaRepository;
import uy.edu.um.adopcion.web.formulario.FiltroMascotas;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogoMascotasServiceTest {

    @Mock
    private MascotaRepository mascotaRepository;

    private CatalogoMascotasService service;
    private Mascota nina;
    private Mascota adulto;
    private Mascota edadDesconocida;

    @BeforeEach
    void setUp() {
        service = new CatalogoMascotasService(mascotaRepository);
        nina = mascota(Especie.PERRO, Sexo.HEMBRA, 6);
        adulto = mascota(Especie.GATO, Sexo.MACHO, 36);
        edadDesconocida = mascota(Especie.GATO, Sexo.DESCONOCIDO, null);
        when(mascotaRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(nina, adulto, edadDesconocida));
    }

    @Test
    void sinFiltrosIncluyeEdadesDesconocidas() {
        assertEquals(List.of(nina, adulto, edadDesconocida), service.buscar(new FiltroMascotas()));
    }

    @Test
    void filtraPorEspecie() {
        FiltroMascotas filtro = new FiltroMascotas();
        filtro.setEspecie(Especie.GATO);

        assertEquals(List.of(adulto, edadDesconocida), service.buscar(filtro));
    }

    @Test
    void filtraPorSexoDesconocido() {
        FiltroMascotas filtro = new FiltroMascotas();
        filtro.setSexo(Sexo.DESCONOCIDO);

        assertEquals(List.of(edadDesconocida), service.buscar(filtro));
    }

    @Test
    void edadMinimaEsInclusivaYExcluyeEdadDesconocida() {
        FiltroMascotas filtro = new FiltroMascotas();
        filtro.setEdadMinima(36);

        assertEquals(List.of(adulto), service.buscar(filtro));
    }

    @Test
    void edadMaximaEsInclusivaYExcluyeEdadDesconocida() {
        FiltroMascotas filtro = new FiltroMascotas();
        filtro.setEdadMaxima(6);

        assertEquals(List.of(nina), service.buscar(filtro));
    }

    @Test
    void combinaTodosLosFiltrosConAnd() {
        FiltroMascotas filtro = new FiltroMascotas();
        filtro.setEspecie(Especie.GATO);
        filtro.setSexo(Sexo.MACHO);
        filtro.setEdadMinima(12);
        filtro.setEdadMaxima(36);

        assertEquals(List.of(adulto), service.buscar(filtro));
    }

    @Test
    void devuelveListaVaciaCuandoNoHayCoincidencias() {
        FiltroMascotas filtro = new FiltroMascotas();
        filtro.setEspecie(Especie.PERRO);
        filtro.setSexo(Sexo.MACHO);

        assertEquals(List.of(), service.buscar(filtro));
    }

    @Test
    void solicitaLosRegistrosOrdenadosPorIdDescendente() {
        ArgumentCaptor<Sort> captor = ArgumentCaptor.forClass(Sort.class);

        service.buscar(new FiltroMascotas());

        verify(mascotaRepository).findAll(captor.capture());
        assertEquals(Sort.Direction.DESC, captor.getValue().getOrderFor("id").getDirection());
    }

    private Mascota mascota(Especie especie, Sexo sexo, Integer edadMeses) {
        return new Mascota(especie, sexo, edadMeses, "https://ejemplo.test/foto");
    }
}
