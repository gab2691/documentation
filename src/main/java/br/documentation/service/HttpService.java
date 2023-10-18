package br.documentation.service;

import br.documentation.exception.AuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
public class HttpService {

    private WebClient webClient;

    @Value("${confluence.uri-login}")
    private String uri;

    @Autowired
    public HttpService(WebClient webClient){
        this.webClient = webClient;
    }

    /**
     * Metodo para baixar documentação do confluence
     *
     * <p>Este método verifica a url de entrada, após a verificação é feita uma chamada GET para resgatar o HTML da página.</p>
     *
     * @param url de entrada do documento.
     * @return o HTML do documento.
     */
    public String getDocFromConfluence(String url) {
        if (url == null || url.isBlank() || url.isEmpty()) {
            log.error("URL is not valid {}", url);
            throw new IllegalArgumentException("Invalid url");
        }
        log.info("Download page from URL {}" , url);
        return webClient
                .get()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.set("Cookie", this.loginConfluence().block().toString()))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }


    /**
     * Método responsavel por ser autenticar na plataforma Confluence.
     *
     * <p>Este método monta um MultiValueMap com as informações necessarias para fazer a chamada HTTP, após isso ele faz a chamada e manipula os cookoies para proximas chamadas</p>
     *
     * @return os cookies do login
     */
    private Mono<StringBuilder> loginConfluence() {
        try {
            return webClient
                    .post()
                    .uri(uri)
                    .headers(httpHeaders -> httpHeaders.set("Content-Type", "application/x-www-form-urlencoded"))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(getStringStringMultiValueMap()))
                    .retrieve()
                    .toEntity(String.class)
                    .flatMap(response -> {
                        return Mono.just(buildCookieString(response));
                    });
        } catch (Exception e) {
            log.error("Error making login to Confluence at URL {}: {}", uri, e.getMessage());
            throw new AuthenticationException("Error trying to authenticate with Confluence", e);
        }
    }

    /**
     * Metodo constroi os cookies vindo da chamada de login
     *
     * <p>Este método recebe uma lista de cookies e constroe uma String unica com eles concatenados</p>
     * @param cookies lista contendo todos os cookies
     * @return String montada
     */
    private StringBuilder buildCookieString(ResponseEntity<String> cookies) {
        StringBuilder cookie = new StringBuilder();
        List<String> cookiesList = cookies.getHeaders().get(HttpHeaders.SET_COOKIE);
        for (String s : cookiesList) {
            cookie = cookie.toString().isEmpty() ? cookie.append(s) : cookie.append("; ").append(s);
        }
        log.info("return cookiesList {}", cookiesList);
        return cookie;
    }

    private MultiValueMap<String, String> getStringStringMultiValueMap() {
        log.info("Contruct map for make HTTP call");
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("os_username", "t734536");
        formData.add("os_cookie", "true");
        formData.add("login", "Autenticação");
        formData.add("os_destination", "");
        formData.add("os_password", "Gbb0914@");
        return formData;
    }
}
