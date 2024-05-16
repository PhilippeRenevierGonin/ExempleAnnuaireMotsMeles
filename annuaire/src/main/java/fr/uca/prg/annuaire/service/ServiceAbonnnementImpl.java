package fr.uca.prg.annuaire.service;

import fr.uca.prg.abonnement.Abonnement;
import fr.uca.prg.annuaire.io.RequetesVersAbonnes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;

@Service
public class ServiceAbonnnementImpl implements ServiceAbonnnement {
    @Autowired
    ServiceEnregistrement enregistrement;

    @Autowired
    RequetesVersAbonnes requetesVersAbonne;

    // clef = url du service auquel quelqu'un est abonné, valeur = liste des services (url) qui y sont abonnés
    // ceux-ci devront avoir le chemin url/serviceeteint
    HashMap<String, HashSet<String>> abonnements = new HashMap<>();

    public void traiterDemandeAbonnement(Abonnement nouvelAbonnement) {
        String abonne = nouvelAbonnement.getId();
        String serviceCible = nouvelAbonnement.getService();
        if ((abonne != null)&&(serviceCible != null)&&enregistrement.estEnregistre(serviceCible)&&enregistrement.estEnregistre(abonne)) {
            if (abonnements.containsKey(serviceCible)) abonnements.get(serviceCible).add(abonne);
            else {
                HashSet<String> abonnes = new HashSet<>();
                abonnes.add(abonne);
                abonnements.put(serviceCible, abonnes);
            }
        }
    }

    public boolean traiterArretService(String serviceUrlQuiSArrete) {
        boolean retire = enregistrement.estEnregistre(serviceUrlQuiSArrete);
        if (retire) {
            enregistrement.oublier(serviceUrlQuiSArrete);

            // s'il y a des abonnés à ce service, on les appelle
            if (abonnements.containsKey(serviceUrlQuiSArrete)) {
                // on récupère la liste, on l'enlève
                HashSet<String> abonnes = abonnements.remove(serviceUrlQuiSArrete);;
                requetesVersAbonne.prevenirAbonne(serviceUrlQuiSArrete, abonnes);
            }

            //  serviceUrl en tant qu'abonné : parcours de toute la collection pour enlever
            for(String serviceAvecAbonnes : abonnements.keySet()) {
                HashSet<String> abonnes = abonnements.get(serviceAvecAbonnes);
                if (abonnes.contains(serviceUrlQuiSArrete)) abonnes.remove(serviceUrlQuiSArrete);
            }
        }
        return retire;
    }
}
