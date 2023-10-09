package br.documentation.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class HttpService {

    @Autowired
    private WebClient webClient;

    public String getDocFromConfluence(String url) {
        if (url == null || url.isBlank() || url.isEmpty()){
            throw new RuntimeException("Invalid url: " + url);
        }
        return webClient
                .get()
                .uri(url)
                .headers(httpHeaders -> httpHeaders.set("Cookie", this.loginConfluence()))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String loginConfluence() {
        List<String> pass = new ArrayList<>(Arrays.asList("Gbb0910@", "Gbb0911@", "Gbb0912@", "Gbb0913@", "Gbb0914@",
                "Gbb0915@", "Gbb0916@", "Gbb0917@", "Gbb0918@", "Gbb0919@", "Gbb0920@", "Gbb0921@", "Gbb0922@", "Gbb0923@"));

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("os_username", "t734536");
        formData.add("os_cookie", "true");
        formData.add("login", "Autenticação");
        formData.add("os_destination", "");

        for (int i = 0; i <= pass.size(); i++) {
            try {
                formData.add("os_password", "Gbb0914@");
                return webClient
                        .post()
                        .uri("https://confluence.santanderbr.corp/dologin.action")
                        .headers(httpHeaders -> httpHeaders.set("Content-Type", "application/x-www-form-urlencoded"))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(BodyInserters.fromFormData(formData))
                        .retrieve()
                        .toEntity(String.class)
                        .flatMap(response -> {
                            String cookie = "";
                            List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
                            for (String s : cookies) {
                                cookie = cookie.isEmpty() ? s : cookie + "; " + s;
                            }
                            return Mono.just(cookie);
                        }).block();
            } catch (Exception e){
                log.error("Error making login, reset request: " + e.getCause());
                continue;
            }
        }
        return "";
    }
}
