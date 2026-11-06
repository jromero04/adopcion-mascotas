package uy.edu.um.adopcion.registro.modelo;

public enum Sexo {
    MACHO("Macho"),
    HEMBRA("Hembra"),
    DESCONOCIDO("Desconocido");

    private final String nombreVisible;

    Sexo(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }
}
