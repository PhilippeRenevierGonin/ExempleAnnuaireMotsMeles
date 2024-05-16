package fr.uca.prg.grille.donnees;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Random;

public class Grille {

    public class Case {
        public int x;
        public int y;

        public Case(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public enum Direction {
        HAUT(0,-1), HAUTDROIT(1, -1), DROIT(1, 0), BASDROIT(1, 1), BAS(0, 1), BASGAUCHE(-1, 1), GAUCHE(-1, 0), HAUTGAUCHE(-1, -1);

        private int x;
        private int y;

        Direction(int x, int y) {
            setX(x);
            setY(y);
        }

        private void setY(int y) {
            this.y = y;
        }

        public int getY() {
            return y;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getX() {
            return x;
        }

        private static final Random PRNG = new SecureRandom();
        public static Direction directionALeatoire()  {
            Direction[] directions = values();
            return directions[PRNG.nextInt(directions.length)];
        }
    }

    private static final String PAS_DE_LETTRE = "_";
    int longueur;
    int hauteur;
    String[][] cases;

    ArrayList<String> liste;

    public int getLongueur() {
        return longueur;
    }

    public void setLongueur(int longueur) {
        this.longueur = longueur;
    }

    public int getHauteur() {
        return hauteur;
    }

    public void setHauteur(int hauteur) {
        this.hauteur = hauteur;
    }

    public String[][] getCases() {
        return cases;
    }

    public void setCases(String[][] cases) {
        this.cases = cases;
    }

    public ArrayList<String> getListe() {
        return liste;
    }

    public void setListe(ArrayList<String> liste) {
        this.liste = liste;
    }

    public Grille() {
        this(10, 10);
    }

    public Grille(int l, int h) {
        this.longueur = l;
        this.hauteur = h;
        cases = new String[h][l];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < l; j++) {
                cases[i][j] = PAS_DE_LETTRE;
            }
        }
        liste = new ArrayList<>();
    }

    public int nbCasesLibres(Case depart, Direction direction) {
        int nbCases = 0;
        int i = depart.x;
        int j = depart.y;
        while ((i >= 0) && (i < longueur) && (j >= 0) && (j < hauteur)) {
            if (cases[j][i].equals(PAS_DE_LETTRE)) {
                nbCases++;
                i = i + direction.getX();
                j = j + direction.getY();
            }
            else break;
        }
        return nbCases;
    }


    public boolean ajouterMot(Case depart, Direction direction, String mot) {
        if ((mot != null) && (mot.length() > 0)) {
            int placelibre = nbCasesLibres(depart, direction);
            String motAjoute = filtrer(mot);
            if (placelibre >= motAjoute.length()) {

                if (motAjoute.length() >= 2) liste.add(mot);

                int i = depart.x;
                int j = depart.y;
                for(int k = 0; k < motAjoute.length(); k++) {
                    cases[j][i] = ""+motAjoute.charAt(k);
                    i = i + direction.getX();
                    j = j + direction.getY();
                }
                return true;
            }
        }

        return false;
    }


    public int nombreCasesVides() {
        int nb = 0;
        for(int i = 0; i < longueur; i++) {
            for(int j = 0; j < hauteur; j++) {
                if (cases[j][i].equals(PAS_DE_LETTRE)) nb++;
            }
        }
        return nb;
    }

    public boolean ajouterMot(int ligne, String mot) {
        if ((mot != null) && (mot.length() > 0) && (ligne < hauteur)) {
            int place = longueur;
            for (String s : cases[ligne]) if (! s.equals(PAS_DE_LETTRE)) place = place - 1;
            String motAjoute = filtrer(mot);
            System.out.println("ligne : " + ligne + " place = " + place + " mot = " + motAjoute);
            boolean ajout = (place >= motAjoute.length());
            if (ajout) {
                int i = 0;
                boolean placeDispo = true;
                while (i < longueur) {
                    // on recherche une case libre
                    while ((i < longueur) && (cases[ligne][i] != PAS_DE_LETTRE)) {
                        i++;
                    }
                    // on regarde s'il y a la place pour le placer
                    int derniereCase = i + motAjoute.length();
                    if (derniereCase > longueur) {  // il n'y a plus de place
                        placeDispo = false;
                        break;
                    }
                    placeDispo = true; // il y a le potentiel de place, on vérifie si les lettres sont libres
                    for (int j = i; j < derniereCase; j++) {
                        placeDispo = placeDispo && cases[ligne][j].equals(PAS_DE_LETTRE);
                        if (!placeDispo) {
                            // il y a une lettre déjà prise
                            i = j; // on mémorise la place pour faire avancer la boucle
                            break;
                        }
                    }
                    if (placeDispo) break;

                }

                ajout = placeDispo; // on met à jour l'action

                if (ajout) {
                    // on change les cases
                    for (int j = 0; j < motAjoute.length(); j++) {
                        cases[ligne][i + j] = "" + motAjoute.charAt(j);
                    }
                    // on ajoute le mot à liste s'il est assez grand
                    if (motAjoute.length() >= 2) liste.add(mot);

                }


            }
            return ajout;
        } else return false;
    }

    public ArrayList<String> listeDesMots() {
        return liste;
    }

    @Override
    public String toString() {
        StringBuilder affichage = new StringBuilder();
        affichage.append(listeDesMots().toString() + "\n");
        for (int i = 0; i < hauteur; i++) {

            for (String m : cases[i]) {
                affichage.append(m);
            }

            int place = longueur;
            for (String s : cases[i]) place = place - s.length();

            for (int j = 0; j < place; j++) affichage.append("_");

            affichage.append("\n");
        }
        return affichage.toString();
    }


    private String filtrer(String mot) {
        String tmp = normalize(mot).toLowerCase();
        return tmp.replaceAll("\\s|-|'", "");
    }

    /**
     * pour enlever et remplacer les lettres accentuées et les ligatures (sauf le w) par des lettres correspondantes
     * le mot est mis en minuscule
     *
     * @param word à normaliser
     * @return le mot normalisé
     */
    private String normalize(String word) {
        String result = word.toLowerCase();

        // les ligatures sont wꝡ
        result = result.replaceAll("ꜳ", "aa");
        result = result.replaceAll("ꜳ", "ae");
        result = result.replaceAll("ꜵ", "ao");
        result = result.replaceAll("ꜷ", "au");
        result = result.replaceAll("ꜹ", "av");
        result = result.replaceAll("ꜻ", "av");
        result = result.replaceAll("ꜽ", "ay");
        result = result.replaceAll("\uD83D\uDE70", "et");
        result = result.replaceAll("ﬀ", "ff");
        result = result.replaceAll("ﬃ", "ffi");
        result = result.replaceAll("ﬄ", "ffl");
        result = result.replaceAll("ﬁ", "fi");
        result = result.replaceAll("ﬂ", "fl");
        result = result.replaceAll("ƕ", "hv");
        result = result.replaceAll("℔", "lb");
        result = result.replaceAll("ỻ", "ll");
        result = result.replaceAll("œ", "oe");
        result = result.replaceAll("ꝏ", "oo");
        result = result.replaceAll("ꭢ", "ɔe");
        result = result.replaceAll("ß", "ſs");
        result = result.replaceAll("ﬆ", "st");
        result = result.replaceAll("ﬅ", "ft");
        result = result.replaceAll("ꜩ", "tz");
        result = result.replaceAll("ᵫ", "ue");
        result = result.replaceAll("ꭣ", "uo");
        result = result.replaceAll("ꝡ", "vy");

        result = Normalizer.normalize(result, Normalizer.Form.NFKD);
        result = result.replaceAll("\\p{M}", "");
        return result;
    }
}
