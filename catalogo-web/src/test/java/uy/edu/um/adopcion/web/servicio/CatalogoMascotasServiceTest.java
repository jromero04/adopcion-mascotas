package uy.edu.um.adopcion.web.servicio;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import uy.edu.um.adopcion.registro.repositorio.MascotaRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CatalogoMascotasServiceTest {

    @Mock
    private MascotaRepository mascotaRepository;

    @Test
    void listaPrimeroLosRegistrosMasRecientes() {
        CatalogoMascotasService service = new CatalogoMascotasService(mascotaRepository);
        ArgumentCaptor<Sort> captor = ArgumentCaptor.forClass(Sort.class);

        service.listar();

        verify(mascotaRepository).findAll(captor.capture());
        Sort.Order orden = captor.getValue().getOrderFor("id");
        assertEquals(Sort.Direction.DESC, orden.getDirection());
    }
}
