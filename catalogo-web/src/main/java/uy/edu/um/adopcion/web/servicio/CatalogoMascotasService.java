package uy.edu.um.adopcion.web.servicio;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uy.edu.um.adopcion.registro.modelo.Mascota;
import uy.edu.um.adopcion.registro.repositorio.MascotaRepository;

import java.util.List;

@Service
public class CatalogoMascotasService {

    private final MascotaRepository mascotaRepository;

    public CatalogoMascotasService(MascotaRepository mascotaRepository) {
        this.mascotaRepository = mascotaRepository;
    }

    @Transactional(readOnly = true)
    public List<Mascota> listar() {
        return mascotaRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }
}
