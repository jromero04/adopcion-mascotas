package uy.edu.um.adopcion.registro.modelo;

public enum Especie {
    PERRO("Perro"),
    GATO("Gato"),
    OTRO("Otro");

    private final String nombreVisible;

    Especie(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }
}
