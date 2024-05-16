package fr.uca.prg.grille.io;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class RequetesMots {
    private WebClient webClient;

    public RequetesMots() {
        this.webClient = WebClient.create("http://localhost:8080");
    }

    public String[] languesDiponibles() {
        return webClient.get().uri("/langues").retrieve().bodyToMono(String[].class).block();
    }

    public String obtenirUnMot(String langue, int longueur) {
        return webClient.get().uri("/"+langue+"/unmot/longueur/"+longueur).retrieve().bodyToMono(String.class).block();
    }

    public Mono<String> obtenirUnMotAsync(String langue, int longueur) {
        return webClient.get().uri("/"+langue+"/unmot/longueur/"+longueur).retrieve().bodyToMono(String.class);
    }

    public void setUrl(String url) {
        webClient = WebClient.create(url);
    }
}
