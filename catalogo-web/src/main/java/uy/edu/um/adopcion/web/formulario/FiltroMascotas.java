package uy.edu.um.adopcion.web.formulario;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.PositiveOrZero;
import uy.edu.um.adopcion.registro.modelo.Especie;
import uy.edu.um.adopcion.registro.modelo.Sexo;

public class FiltroMascotas {

    private Especie especie;
    private Sexo sexo;

    @PositiveOrZero(message = "La edad mínima no puede ser negativa")
    private Integer edadMinima;

    @PositiveOrZero(message = "La edad máxima no puede ser negativa")
    private Integer edadMaxima;

    @AssertTrue(message = "La edad mínima no puede ser mayor que la edad máxima")
    public boolean isRangoValido() {
        return edadMinima == null || edadMaxima == null || edadMinima <= edadMaxima;
    }

    public boolean isActivo() {
        return especie != null || sexo != null || edadMinima != null || edadMaxima != null;
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

    public Integer getEdadMinima() {
        return edadMinima;
    }

    public void setEdadMinima(Integer edadMinima) {
        this.edadMinima = edadMinima;
    }

    public Integer getEdadMaxima() {
        return edadMaxima;
    }

    public void setEdadMaxima(Integer edadMaxima) {
        this.edadMaxima = edadMaxima;
    }
}
