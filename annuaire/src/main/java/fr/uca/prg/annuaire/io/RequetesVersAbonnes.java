package fr.uca.prg.annuaire.io;

import java.util.Set;

public interface RequetesVersAbonnes {
    void prevenirAbonne(String urlServiceEteint, Set<String> abonnesAPrevenir);
}
