package uy.edu.um.adopcion.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import uy.edu.um.adopcion.registro.modelo.Especie;
import uy.edu.um.adopcion.registro.modelo.Sexo;
import uy.edu.um.adopcion.registro.repositorio.MascotaRepository;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:adopcion-integracion;DB_CLOSE_DELAY=-1",
                "spring.jpa.hibernate.ddl-auto=create-drop"
        }
)
class MascotaApplicationIT {

    @LocalServerPort
    private int puerto;

    @Autowired
    private MascotaRepository mascotaRepository;

    private HttpClient cliente;

    @BeforeEach
    void prepararPrueba() {
        mascotaRepository.deleteAll();
        cliente = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    @Test
    void registraPersisteYMuestraLaMascotaEnElCatalogoYLaBusqueda() throws Exception {
        HttpResponse<String> registro = enviarFormulario(
                "nombre=Luna"
                        + "&especie=PERRO"
                        + "&sexo=HEMBRA"
                        + "&edadMeses=12"
                        + "&descripcion=" + codificar("Encontrada cerca de la plaza")
                        + "&fotoUrl=" + codificar("https://ejemplo.test/luna")
        );

        assertEquals(302, registro.statusCode());
        URI redireccion = URI.create(registro.headers().firstValue("Location").orElseThrow());
        assertEquals("/mascotas", redireccion.getPath());
        assertEquals("registrada", redireccion.getQuery());

        var guardadas = mascotaRepository.findAll();
        assertEquals(1, guardadas.size());
        assertEquals("Luna", guardadas.getFirst().getNombre());
        assertEquals(Especie.PERRO, guardadas.getFirst().getEspecie());
        assertEquals(Sexo.HEMBRA, guardadas.getFirst().getSexo());
        assertEquals(12, guardadas.getFirst().getEdadMeses());

        HttpResponse<String> coincidencia = consultar(
                "/mascotas?especie=PERRO&sexo=HEMBRA&edadMinima=12&edadMaxima=12"
        );
        assertEquals(200, coincidencia.statusCode());
        assertTrue(coincidencia.body().contains("Luna"));
        assertTrue(coincidencia.body().contains("https://ejemplo.test/luna"));

        HttpResponse<String> sinCoincidencia = consultar("/mascotas?especie=GATO");
        assertEquals(200, sinCoincidencia.statusCode());
        assertFalse(sinCoincidencia.body().contains("Luna"));
        assertTrue(sinCoincidencia.body().contains("No encontramos mascotas con esos filtros"));
    }

    private HttpResponse<String> enviarFormulario(String cuerpo) throws Exception {
        HttpRequest solicitud = HttpRequest.newBuilder(uri("/mascotas"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo))
                .build();
        return cliente.send(solicitud, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> consultar(String ruta) throws Exception {
        HttpRequest solicitud = HttpRequest.newBuilder(uri(ruta)).GET().build();
        return cliente.send(solicitud, HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String ruta) {
        return URI.create("http://127.0.0.1:" + puerto + ruta);
    }

    private String codificar(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }
}
