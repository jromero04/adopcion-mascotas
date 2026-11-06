package uy.edu.um.adopcion.registro.servicio;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uy.edu.um.adopcion.registro.modelo.Mascota;
import uy.edu.um.adopcion.registro.repositorio.MascotaRepository;

import java.util.Objects;

@Service
public class RegistroMascotaService {

    private final MascotaRepository mascotaRepository;
    private final Validator validator;

    public RegistroMascotaService(MascotaRepository mascotaRepository, Validator validator) {
        this.mascotaRepository = mascotaRepository;
        this.validator = validator;
    }

    @Transactional
    public Mascota registrar(Mascota mascota) {
        Objects.requireNonNull(mascota, "La mascota es obligatoria");
        mascota.normalizarDatosOpcionales();

        var errores = validator.validate(mascota);
        if (!errores.isEmpty()) {
            throw new ConstraintViolationException(errores);
        }

        return mascotaRepository.save(mascota);
    }
}
