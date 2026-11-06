package uy.edu.um.adopcion.web.servicio;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uy.edu.um.adopcion.registro.modelo.Mascota;
import uy.edu.um.adopcion.registro.repositorio.MascotaRepository;
import uy.edu.um.adopcion.web.formulario.FiltroMascotas;

import java.util.List;

@Service
public class CatalogoMascotasService {

    private final MascotaRepository mascotaRepository;

    public CatalogoMascotasService(MascotaRepository mascotaRepository) {
        this.mascotaRepository = mascotaRepository;
    }

    @Transactional(readOnly = true)
    public List<Mascota> buscar(FiltroMascotas filtro) {
        return mascotaRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .filter(mascota -> filtro.getEspecie() == null || mascota.getEspecie() == filtro.getEspecie())
                .filter(mascota -> filtro.getSexo() == null || mascota.getSexo() == filtro.getSexo())
                .filter(mascota -> coincideEdad(mascota, filtro))
                .toList();
    }

    private boolean coincideEdad(Mascota mascota, FiltroMascotas filtro) {
        if (filtro.getEdadMinima() == null && filtro.getEdadMaxima() == null) {
            return true;
        }
        if (mascota.getEdadMeses() == null) {
            return false;
        }
        return (filtro.getEdadMinima() == null || mascota.getEdadMeses() >= filtro.getEdadMinima())
                && (filtro.getEdadMaxima() == null || mascota.getEdadMeses() <= filtro.getEdadMaxima());
    }
}
