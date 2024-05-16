package fr.uca.prg.grille.io;

import fr.uca.prg.abonnement.BaseWebControlleur;
import fr.uca.prg.grille.donnees.Grille;
import fr.uca.prg.grille.service.GenerateurGrille;
import fr.uca.prg.grille.service.StockageGrille;
import fr.uca.prg.grille.verification.VerificateurGrille;
import fr.uca.prg.verification.AttenteVerification;
import fr.uca.prg.verification.ServeurPasPret;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

@CrossOrigin
@RestController
public class GrilleRestController extends BaseWebControlleur implements AttenteVerification {

    @Autowired
    VerificateurGrille verificateur;
    @Autowired
    StockageGrille stockageGrille;
    @Autowired
    RequetesMots requetesMots;
    @Autowired
    GenerateurGrille generateurGrille;

    public GrilleRestController() {
    }

    @Override
    public void changementEtatVerification(boolean pret) {
        // simplification du test
        if (pret  /* && (requetesMots != null) &&  (stockageGrille != null) */ )   {
            requetesMots.setUrl(verificateur.getUrlWordsService());
            final String uuid = stockageGrille.ajouterGrille(null);

            Thread lancementEnTacheDeFond = new Thread(() -> {
                    Grille g = generateurGrille.creer("fr", 10, 10);
                    System.out.println("*************** une grille pré-enregistré (on peut l'obtenir) ***************** "+Thread.currentThread().getName());
                    stockageGrille.mettreAJour(uuid, g);
                    System.out.println("Grille pré-enregistrée : "+uuid);
                    String monUrl = "http://localhost:8081/";
                    try {
                        monUrl=verificateur.obtenirMaPropreAdresse();
                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println(monUrl+"grille/"+uuid);
                    System.out.println("************* on réserve une grille (on ne peut pas l'obtenir) *******************");
                    String uuidTmp = stockageGrille.ajouterGrille(null);
                    System.out.println("Grille réservée mais incomplète : "+uuidTmp);
                    System.out.println(monUrl+"/grille/"+uuidTmp);
            });
            lancementEnTacheDeFond.start();

        }
    }


    public void finInit() {
        System.out.println("~~~~~~~~~~~~~ requetesMots "+requetesMots);
        verificateur.ajouterAttenteVerification(this);

        verificateur.verifierLangue();
        verificateur.verifierContrat();
    }


    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "génération d'une grille avec des requêtes bloquantes"),
            @ApiResponse(responseCode = "503", description = "serveur pas prêt")})
    @GetMapping("/unegrille-3a")
    public Grille uneGrille3a(@RequestParam(required = false) Integer longueur, @RequestParam(required = false) Integer hauteur) {
        if ( ! verificateur.estPret())throw new ServeurPasPret();

        Grille grille = new Grille();
        int l = 10;
        int h = 10;
        if ((longueur != null) && (longueur > 0)) {
            l = longueur;
        }
        if ((hauteur != null) && (hauteur > 0)) {
            h = hauteur;
        }

        String[] langues = requetesMots.languesDiponibles();
        boolean containsFr = Arrays.stream(langues).anyMatch("fr"::equals);
        if (containsFr) {
            grille = generateurGrille.creer("fr", l, h);
        }
        return grille;
    }


    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "génération d'une grille pré-généré, les paramètres ne servent à rien"),
            @ApiResponse(responseCode = "503", description = "serveur pas prêt")})
    @GetMapping("/unegrille-3b")
    public Grille uneGrille3b(@RequestParam(required = false) Integer longueur, @RequestParam(required = false) Integer hauteur) {
        if ( ! verificateur.estPret()) throw new ServeurPasPret();
        return stockageGrille.obtenirUneGrille();
    }



    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "obtenir une grille à partir de son uuid"),
            @ApiResponse(responseCode = "206", description = "grille en cours de génération"),
            @ApiResponse(responseCode = "404", description = "uuid de grille inconnu"),
            @ApiResponse(responseCode = "503", description = "serveur pas prêt")
    })
    @GetMapping("/grille/{uuid}")
    public Grille grilleViaUUID(@PathVariable String uuid)  {
        if ( ! verificateur.estPret()) throw new ServeurPasPret();
        return stockageGrille.obtenirGrille(uuid);
    }

    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "demande d'une génération d'une grille en fonction des paramètres spécifiés, retourne un uuid"),
            @ApiResponse(responseCode = "503", description = "serveur pas prêt")})
    @GetMapping("/grille/nouvelle")
    public String commanderGrille(@RequestParam String langue, @RequestParam int longueur, @RequestParam int hauteur) {
        if ( ! verificateur.estPret()) throw new ServeurPasPret();

        if ((longueur > 2) && (hauteur > 2)) {
            return generateurGrille.genererGrilleEnTacheDeFond(langue, longueur, hauteur);
        }
        else return "taille insuffisante";
    }


    @Override
    protected void serviceQuiDisparait(String url) {
        System.out.println("------> serviceQuiDisparait : "+url+ " / "+verificateur.getUrlWordsService());
        if ((url != null) && (url.equals(verificateur.getUrlWordsService()))) {
            verificateur.reInitialiseLangue();
        }
    }



    /* *********** une grille générée à la volée ********** */
    /* *********** ce code pourrait être découpé ********** */
    long timing = 0;
    private Random rand = new SecureRandom();
    private String[] lettres = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "obtenir une grille générée à la volée avec des requêtes asynchrones"),
            @ApiResponse(responseCode = "503", description = "serveur pas prêt")
    })@GetMapping("grille/instantannee")
    public Mono<Grille> uneGrille(@RequestParam String langue, @RequestParam int longueur, @RequestParam int hauteur, @RequestParam(defaultValue ="100") int nbEchecMax ) {
        if (verificateur.estPret()) {
            timing = System.currentTimeMillis();
            Grille futureGrille = new Grille(longueur, hauteur);
            return generer(langue, longueur, hauteur, futureGrille, 0, 0, nbEchecMax);
        }
        else {
            throw new ServeurPasPret();
        }
    }

    private String uneLettre() {
        return lettres[rand.nextInt(lettres.length)];
    }

    public Mono<Grille> generer(String langue, int longueur, int hauteur, Grille grille, int nbRequetes, int nbEchecs, final int nbEchecMax) {
        int nbPlaces = grille.nombreCasesVides();
        // on limite l'effet du hasard sur la duree de la boucle
        while ((nbPlaces > 7) && (nbEchecs <nbEchecMax) ) {
            Grille.Direction d = Grille.Direction.directionALeatoire();
            int i = rand.nextInt(longueur);
            int j = rand.nextInt(hauteur);
            Grille.Case c = grille.new Case(i, j);
            int n = grille.nbCasesLibres(c, d);
            if (n >= 4) { // des mots pas trop petits
                final int nbE = nbEchecs;
                if (n > 10) n = rand.nextInt(8, Math.min(20, n+1)); // limite de la taille des mots
                return requetesMots.obtenirUnMotAsync(langue, n).flatMap(m -> {
                    grille.ajouterMot(c, d, m);
                    return generer(langue, longueur, hauteur, grille, nbRequetes+1, nbE, nbEchecMax);
                });
            } else {
                nbEchecs++;
            }
            nbPlaces = grille.nombreCasesVides();
        }

        System.out.println("avant complétion");
        System.out.println("nbE = "+nbEchecs);
        System.out.println("nbR = "+nbRequetes);

        return completerGrille(langue, longueur, hauteur, grille, nbRequetes, 0, 0);
        // return Mono.just(grille);
    }


    private Mono<Grille> completerGrille(String langue, int longueur, int hauteur, Grille grille, int nbRequetes, int j, int i) {
        int initLettre = i;
        // on complete pour etre sur (on pourrait aussi faire vers la gauche ou tirer au hasard entre gauche et droite pour chaque ligne
        // ou encore tirer au hasard entre remplissage vertical ou horizontal...
        for(int ligne = j; ligne < hauteur; ligne++) {
            for(int lettre =initLettre; lettre < longueur; lettre++) {
                Grille.Case c = grille.new Case(lettre, ligne);
                int n = grille.nbCasesLibres(c, Grille.Direction.DROIT);
                if (n >= 2) {
                    final int numeroLigne =  lettre+n >= longueur ? ligne+1 : ligne ;
                    final int indiceLettre =  lettre+n >= longueur ? 0 : lettre+n ;
                    if (n > 10) n = rand.nextInt(8, Math.min(20, n+1)); // limite de la taille des mots
                    return requetesMots.obtenirUnMotAsync(langue, n).flatMap(m -> {
                        grille.ajouterMot(c, Grille.Direction.DROIT, m);
                        return completerGrille(langue, longueur, hauteur, grille, nbRequetes+1, numeroLigne,  indiceLettre);
                    });
                } else if (n ==1 )  {
                    grille.ajouterMot(c, Grille.Direction.DROIT, uneLettre());
                }
            }
            initLettre = 0;
        }

        System.out.println("apres complétion");
        System.out.println("nbR = "+nbRequetes);
        System.out.println(grille);
        System.out.println("timing = "+(System.currentTimeMillis()-timing));
        return Mono.just(grille);
    }

}
