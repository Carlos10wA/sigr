package co.edu.tecnologicaoriente.sigr.controller;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:sigrseguridad;DB_CLOSE_DELAY=-1",
        "spring.h2.console.enabled=false"
    }
)
class SeguridadHttpIntegrationTest {

    @LocalServerPort
    private int puerto;

    @Test
    void postSinCsrfDevuelve403EnServidorReal() throws Exception {
        try (HttpClient cliente = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build()) {

            HttpRequest solicitud = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + puerto + "/usuarios"))
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> respuesta = cliente.send(
                    solicitud, HttpResponse.BodyHandlers.ofString());

            assertThat(respuesta.statusCode()).isEqualTo(403);
        }
    }
}