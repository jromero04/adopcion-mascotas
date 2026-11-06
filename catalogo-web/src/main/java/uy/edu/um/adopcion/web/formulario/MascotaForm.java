package uy.edu.um.adopcion.web.formulario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import uy.edu.um.adopcion.registro.modelo.Especie;
import uy.edu.um.adopcion.registro.modelo.Mascota;
import uy.edu.um.adopcion.registro.modelo.Sexo;

public class MascotaForm {

    private String nombre;

    @NotNull(message = "Seleccioná una especie")
    private Especie especie;

    @NotNull(message = "Seleccioná un sexo")
    private Sexo sexo = Sexo.DESCONOCIDO;

    @PositiveOrZero(message = "La edad aproximada no puede ser negativa")
    private Integer edadMeses;

    private String descripcion;

    @NotBlank(message = "La URL de la foto es obligatoria")
    @Pattern(
            regexp = "(?i)^https?://[^\\s]+$",
            message = "La URL debe comenzar con http:// o https://"
    )
    private String fotoUrl;

    public Mascota aMascota() {
        Mascota mascota = new Mascota(especie, sexo, edadMeses, fotoUrl);
        mascota.setNombre(nombre);
        mascota.setDescripcion(descripcion);
        return mascota;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Especie getEspecie() {
        return especie;
    }

    public void setEspecie(Especie especie) {
        this.especie = especie;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }

    public Integer getEdadMeses() {
        return edadMeses;
    }

    public void setEdadMeses(Integer edadMeses) {
        this.edadMeses = edadMeses;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }
}
