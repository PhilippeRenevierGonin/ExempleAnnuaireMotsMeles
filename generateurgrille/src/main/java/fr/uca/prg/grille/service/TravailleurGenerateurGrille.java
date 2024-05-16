package fr.uca.prg.grille.service;

import fr.uca.prg.grille.donnees.Grille;
import fr.uca.prg.grille.io.RequetesMots;

import java.util.Arrays;

public class TravailleurGenerateurGrille implements Runnable {
    private final String langue;
    private final int longueur;
    private final int hauteur;
    private final StockageGrille stockageGrille;
    private final RequetesMots requetesMots;
    private final GenerateurGrille generateurGrille;

    private String uuid = "";
    Thread thread = null ;

    public TravailleurGenerateurGrille(String langue, int longueur, int hauteur, StockageGrille stock, RequetesMots requetesMots, GenerateurGrille generateurGrille) {
        this.langue = langue;
        this.longueur = longueur;
        this.hauteur=  hauteur;
        this.stockageGrille = stock;
        this.requetesMots = requetesMots;
        this.generateurGrille = generateurGrille;
    }

    public String genereEnTacheDeFond() {
        uuid = stockageGrille.reserver();
        if (thread == null) {
             thread = new Thread(this);
             thread.start();
        }
        return uuid;
    }

    @Override
    public void run() {
        Grille grille = null;
        String[] langues = requetesMots.languesDiponibles();
        boolean containsFr = Arrays.stream(langues).anyMatch(langue::equals);
        if (containsFr) {
            grille = generateurGrille.creer(langue, longueur, hauteur);
            stockageGrille.mettreAJour(uuid, grille);
        } else {
            stockageGrille.liberer(uuid);
        }

    }
}
