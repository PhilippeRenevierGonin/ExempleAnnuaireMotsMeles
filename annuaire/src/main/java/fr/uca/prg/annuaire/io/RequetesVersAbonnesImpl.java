package fr.uca.prg.annuaire.io;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Set;

@Component
public class RequetesVersAbonnesImpl implements RequetesVersAbonnes {

    WebClient webClient = WebClient.create();

    @Override
    public void prevenirAbonne(String urlServiceEteint, Set<String> abonnesAPrevenir) {
        new Thread(() -> {
            // il faudrait un bloc synchro pour modifier abonnements
            for(String urlAbonne : abonnesAPrevenir) {
                // subscribe ou block
                System.out.println("-----> on previent "+urlAbonne+" pour "+urlServiceEteint);
                webClient.post().uri(urlAbonne+"/serviceeteint").body(BodyInserters.fromValue(urlServiceEteint)).retrieve().bodyToMono(Void.class).subscribe();
            }
        }).start();
    }
}
