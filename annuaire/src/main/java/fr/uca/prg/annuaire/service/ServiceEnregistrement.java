package fr.uca.prg.annuaire.service;

import fr.uca.prg.verification.Identification;

public interface ServiceEnregistrement {
    boolean enregistrerNouveauService(Identification nouveauService);

    String[] getListeDesServices();

    String[] rechercherAvecUnChemin(String chemin);

    String[] rechercherAvecPlusieursChemins(String[] chemins);

    boolean estEnregistre(String url);

    void oublier(String url);
}
