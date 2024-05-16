package fr.uca.prg.grille.service;

import fr.uca.prg.grille.donnees.Grille;
import fr.uca.prg.grille.io.RequetesMots;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Random;

@Service
public class GenerateurGrille {
    @Autowired
    private RequetesMots serviceDistant;

    @Autowired
    private StockageGrille stockageGrille;


    private Random rand = new SecureRandom();
    private String[] lettres = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

    public GenerateurGrille() {
    }


    public String genererGrilleEnTacheDeFond(String langue, int longueur, int hauteur) {
        TravailleurGenerateurGrille travailleur = new TravailleurGenerateurGrille(langue, longueur, hauteur, stockageGrille, serviceDistant, this);
        return travailleur.genereEnTacheDeFond();
    }

    public Grille creer(String langue, int longueur, int hauteur) {
        System.out.println("~~~~~~~~~~~~~ serviceDistant "+serviceDistant);

        Grille grille = new Grille(longueur, hauteur);

        int nbRequetes = 0;

            int nbPlaces = grille.nombreCasesVides();
            int nbEchecs = 0;
            // on limite l'effet du hasard sur la duree de la boucle
            while ((nbPlaces > 7) && (nbEchecs <40) ) {
                Grille.Direction d = Grille.Direction.directionALeatoire();
                int i = rand.nextInt(longueur);
                int j = rand.nextInt(hauteur);
                Grille.Case c = grille.new Case(i, j);
                int n = grille.nbCasesLibres(c, d);
                if (n >= 4) { // des mots pas trop petits
                    if (n > 10) n = rand.nextInt(8, Math.min(20, n+1)); // limite de la taille des mots
                    String mot = serviceDistant.obtenirUnMot(langue, n);
                    nbRequetes++;
                    grille.ajouterMot(c, d, mot);
                } else {
                    nbEchecs++;
                }
                nbPlaces = grille.nombreCasesVides();
            }
            // on complete pour etre sur (on pourrait aussi faire vers la gauche ou tirer au hasard entre gauche et droite pour chaque ligne
            // ou encore tirer au hasard entre remplissage vertical ou horizontal...
            for(int j = 0; j < hauteur; j++) {
                for(int i = 0; i < longueur; i++) {
                    Grille.Case c = grille.new Case(i, j);
                    int n = grille.nbCasesLibres(c, Grille.Direction.DROIT);
                    if (n >= 2) {
                        if (n > 10) n = rand.nextInt(8, Math.min(20, n+1)); // limite de la taille des mots
                        String mot = serviceDistant.obtenirUnMot(langue, n);
                        nbRequetes++;
                        grille.ajouterMot(c, Grille.Direction.DROIT, mot);
                    } else if (n ==1 )  {
                        grille.ajouterMot(c, Grille.Direction.DROIT, uneLettre());
                    }
                }
            }

            System.out.println(nbRequetes);
            return grille;
    }


    private String uneLettre() {
        return lettres[rand.nextInt(lettres.length)];
    }



}
