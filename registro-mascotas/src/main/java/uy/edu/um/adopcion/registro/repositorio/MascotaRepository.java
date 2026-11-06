package uy.edu.um.adopcion.registro.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import uy.edu.um.adopcion.registro.modelo.Mascota;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {
}
