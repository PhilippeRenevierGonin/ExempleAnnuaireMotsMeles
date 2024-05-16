package fr.uca.prg.annuaire.service;

import fr.uca.prg.abonnement.Abonnement;

public interface ServiceAbonnnement {
    void traiterDemandeAbonnement(Abonnement nouvelAbonnement);
    boolean traiterArretService(String serviceUrlQuiSArrete);

}
