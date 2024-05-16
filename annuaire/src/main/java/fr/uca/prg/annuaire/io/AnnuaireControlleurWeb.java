package fr.uca.prg.annuaire.io;

import fr.uca.prg.abonnement.Abonnement;
import fr.uca.prg.annuaire.service.ServiceAbonnnement;
import fr.uca.prg.annuaire.service.ServiceEnregistrement;
import fr.uca.prg.verification.Identification;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@RestController
public class AnnuaireControlleurWeb {
    @Autowired
    ServiceEnregistrement enregistrement;

    @Autowired
    ServiceAbonnnement abonnement;

    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "renvoie true si l'enregistrement est possible, un seul enregistrement par url. C'est contenu dans Identification")})
    @PostMapping("/senregistrer")
    public boolean enregistrement(@RequestBody Identification nouveauService) {
        return enregistrement.enregistrerNouveauService(nouveauService);
    }

    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "renvoie la liste des services (leur URL) qui se sont enregistrés")})
    @GetMapping("/services")
    public String[] listerLesService() {
        return enregistrement.getListeDesServices();
    }

    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "recherche et renvoie la liste des services qui ont un chemin qui correspond à la requête")})
    @GetMapping("rechercher")
    public String[] rechercher(@RequestParam String chemin) {
        return enregistrement.rechercherAvecUnChemin(chemin);
    }

    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "recherche et renvoie la liste des services qui ont des chemins qui correspondent à la requête")})
    @GetMapping("rechercher/multiples")
    public String[] rechercherMultiplesChemins(@RequestParam String[] chemins) {
        return enregistrement.rechercherAvecPlusieursChemins(chemins);
    }

    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "pour retirer un service de l'annuaire")})
    @DeleteMapping("/seffacer/{url}")
    public boolean dereferrencement(@PathVariable String url) throws UnsupportedEncodingException {
        String serviceUrl = URLDecoder.decode(url, StandardCharsets.UTF_8.toString());

        return abonnement.traiterArretService(serviceUrl);
    }

    @PostMapping( "/sabonner")
    public void sAbonner(@RequestBody Abonnement nouvelAbonnement) {
        abonnement.traiterDemandeAbonnement(nouvelAbonnement);
    }

}
