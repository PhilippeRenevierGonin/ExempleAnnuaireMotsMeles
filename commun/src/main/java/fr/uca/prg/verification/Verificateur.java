package fr.uca.prg.verification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uca.prg.abonnement.Abonnement;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.annotation.PreDestroy;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public abstract class Verificateur {

    /*** webClient **/
    private String urlAnnuaire = "http://localhost:8024/";
    private WebClient webClient = WebClient.create(urlAnnuaire);

    /*** ce qui est lié au contrat **/
    protected OpenAPI contrat;

    protected boolean contratPret = false;

    public OpenAPI getContrat() {
        return contrat;
    }

    public void setContrat(OpenAPI contrat) {
        this.contrat = contrat;
    }

    public boolean isContratPret() {
        return contratPret;
    }

    public void setContratPret(boolean contratPret) {
        this.contratPret = contratPret;
    }

    public abstract boolean estPret();


    /*** gestion de l'url **/
    public abstract Environment getEnvironment();

    public String obtenirMaPropreAdresse() throws UnknownHostException {
        final String port = getEnvironment().getProperty("local.server.port") == null ? "80" : getEnvironment().getProperty("local.server.port") ;
        final String ip = InetAddress.getLocalHost().getHostAddress();
        return "http://"+ip+":"+port+"/";
    }

    /*** l'abonnement **/
    private ArrayList<AttenteVerification> ecouteurs = new ArrayList<>();
    public void ajouterAttenteVerification(AttenteVerification enAttente) {
        this.ecouteurs.add(enAttente);
    }

    // méthode interne pour prévenir les écouteurs
    protected void prevenirEcouteurs() {
        for(AttenteVerification a : ecouteurs) a.changementEtatVerification(estPret() );
    }


    /*** échange avec l'annuaire **/
    // pour lancer la vérification (la récupération) du contrat
    public void verifierContrat() {
        try {
            obtenirLeContrat(5);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }


    // méthode interne, qui va se rappeler "tentativeRestante" fois en cas d'échec
    // il y aura des échecs, car au débutn le numéro de port n'est pas modifié.
    private void obtenirLeContrat(int tentativeRestante) throws UnknownHostException {
        final String monUrl = obtenirMaPropreAdresse();
        System.out.println(" ------------> on essaie de récupérer le contrat sur "+monUrl+"tentative : "+tentativeRestante);

        WebClient webClient = WebClient.create(monUrl);
        webClient.get().uri("/v3/api-docs").retrieve().bodyToMono(String.class).onErrorResume(err -> {
            System.out.println(err);
            try {
                TimeUnit.MILLISECONDS.sleep(250);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (tentativeRestante > 0) {
                try {
                    obtenirLeContrat(tentativeRestante - 1);
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                contratPret = false;
                System.err.println("=====> obtenirLeContrat =====> echec" );
                prevenirEcouteurs();
            }
            return Mono.empty();
        }).subscribe(json -> {
            ObjectMapper mapper = new ObjectMapper();
            try {
                contrat = mapper.readValue(json, OpenAPI.class);
                Identification monId = new Identification(monUrl, contrat);
                webClient.post().uri(urlAnnuaire+"/senregistrer").accept(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(monId)).retrieve().bodyToMono(Boolean.class).subscribe(
                        accepte -> {
                            System.out.println("on a été accepté !");
                            contratPret = true;
                            prevenirEcouteurs();
                        });

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    protected Mono<String[]> rechercheService(String... chemins) {
        return webClient.get().uri("/rechercher/multiples", uri -> uri.queryParam("chemins", chemins).build()).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String[].class);
    }

    protected Mono<String[]> rechercheService(String[] chemins, String[] pathVariables) {
        return webClient.get().uri("/rechercher/multiples", uri -> uri.queryParam("chemins", chemins).build(pathVariables)).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String[].class);
    }

    @PreDestroy
    public void fin() throws UnknownHostException {
        if (contrat != null) {
            String monUrl = obtenirMaPropreAdresse();
            try {
                monUrl = URLEncoder.encode(monUrl, StandardCharsets.UTF_8.toString());
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            WebClient webClient = WebClient.create(urlAnnuaire);
            webClient.delete().uri("/seffacer/"+monUrl).retrieve().bodyToMono(Boolean.class).block();
        }
    }

    protected void sAbonner(String serviceUrl) {
        new Thread(() -> {
            try {
                while (! isContratPret()) {
                    System.out.println("en attente du contrat... (attente du port...) ");
                    try {
                        TimeUnit.MILLISECONDS.sleep(250);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                System.out.println("sAbonner, on fait la requete");
                String monUrl = obtenirMaPropreAdresse();
                // avec subscribe ou block, ici peu important
                webClient.post().uri("/sabonner").body(BodyInserters.fromValue(new Abonnement(monUrl, serviceUrl))).retrieve().bodyToMono(Void.class).subscribe();
                System.out.println("sAbonner, on a fini la requete");

            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void setUrlAnnuaire(String url) {
        urlAnnuaire = url;
        webClient = WebClient.create(urlAnnuaire);
    }
}
