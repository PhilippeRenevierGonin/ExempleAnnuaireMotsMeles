package fr.uca.prg.grille.service;

import fr.uca.prg.grille.donnees.Grille;
import fr.uca.prg.grille.donnees.exception.GrilleNonTrouvee;
import fr.uca.prg.grille.donnees.exception.GrillePasEncorePrete;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.UUID;

@Component
public class StockageGrille {
    HashMap<String, Grille> grillesMemorisees = new HashMap();

    public Grille obtenirUneGrille() {
        if (grillesMemorisees.size() > 0) {
            int i = 0;
            while( i < grillesMemorisees.size() ) {
                Grille g = (Grille) grillesMemorisees.values().toArray()[i];
                if (g == null) i++;
                else return g;
            }
        }
        // else // il n'y a pas de grille enregistrée ou alors elles sont toutes null
        return new Grille();
    }

    public synchronized String ajouterGrille(Grille grille) {
        UUID uuid = UUID.randomUUID();
        while (grillesMemorisees.containsKey(uuid.toString())) {
            uuid = UUID.randomUUID();
        }
        grillesMemorisees.put(uuid.toString(), grille);
        return uuid.toString();
    }

    public Grille obtenirGrille(String uuid) throws GrilleNonTrouvee, GrillePasEncorePrete {
        if (grillesMemorisees.containsKey(uuid)) {
            Grille g = grillesMemorisees.get(uuid);
            if (g == null) throw new GrillePasEncorePrete();
            else return g;
        }
        else throw new GrilleNonTrouvee();
    }


    public synchronized String reserver() {
        return ajouterGrille(null);
    }

    public synchronized boolean liberer(String uuid) {
        if (grillesMemorisees.containsKey(uuid)) {
            Grille oldG = grillesMemorisees.get(uuid);
            if (oldG == null) {
                grillesMemorisees.remove(uuid);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean mettreAJour(String uuid, Grille g) {
        if (grillesMemorisees.containsKey(uuid)) {
            Grille oldG = grillesMemorisees.get(uuid);
            if (oldG == null) {
                grillesMemorisees.put(uuid, g);
                return true;
            }
        }
        return false;
    }
}
