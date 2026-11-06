package uy.edu.um.adopcion.registro.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "mascotas")
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String nombre;

    @NotNull(message = "La especie es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Especie especie;

    @NotNull(message = "El sexo es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Sexo sexo = Sexo.DESCONOCIDO;

    @PositiveOrZero(message = "La edad aproximada no puede ser negativa")
    private Integer edadMeses;

    @Column(length = 1000)
    private String descripcion;

    @NotBlank(message = "La URL de la foto es obligatoria")
    @Pattern(
            regexp = "(?i)^https?://[^\\s]+$",
            message = "La URL de la foto debe comenzar con http:// o https://"
    )
    @Column(nullable = false, length = 2048)
    private String fotoUrl;

    protected Mascota() {
    }

    public Mascota(Especie especie, Sexo sexo, Integer edadMeses, String fotoUrl) {
        this.especie = especie;
        this.sexo = sexo;
        this.edadMeses = edadMeses;
        this.fotoUrl = fotoUrl;
    }

    public void normalizarDatosOpcionales() {
        nombre = textoOpcional(nombre);
        descripcion = textoOpcional(descripcion);
        fotoUrl = fotoUrl == null ? null : fotoUrl.trim();
        if (sexo == null) {
            sexo = Sexo.DESCONOCIDO;
        }
    }

    private String textoOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    public Long getId() {
        return id;
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
