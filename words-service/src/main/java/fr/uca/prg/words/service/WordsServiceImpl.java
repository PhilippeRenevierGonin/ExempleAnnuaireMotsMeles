package fr.uca.prg.words.service;

import fr.uca.prg.words.dico.Dictionnary;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

@Service public class WordsServiceImpl implements WordsService {

    @Autowired
    DicoImportExport dicoIE;

    SecureRandom rand = new SecureRandom();


    private void lookingForAnagrams(String word, Dictionnary dico, HashSet<String> possibleAnagrams) {
        String key = dico.anagram(word);

        ArrayList<int[]> subwordIndexes = new ArrayList<>();

        // on récupère toutes les combinaisons des indices des lettres, donc toutes les combinaisons des lettres
        for (int i = 1; i <= key.length(); i++) {
            Iterator<int[]> list = CombinatoricsUtils.combinationsIterator(key.length(), i);
            while (list.hasNext()) {
                final int[] combination = list.next();
                subwordIndexes.add(combination);
            }
        }

        // pour chaque combinaison, on cumule les anagrammes dans le "Set", pour ne pas avoir de doublon
        for (int i = 0; i < subwordIndexes.size(); i++) {
            int[] indexes = subwordIndexes.get(i);
            String anagram = "";
            for (int j = 0; j < indexes.length; j++) anagram += key.charAt(indexes[j]);

            ArrayList<String> adds = dico.getDictionnary().get(anagram);
            if ((adds != null) && (adds.size() > 0)) possibleAnagrams.addAll(adds);

        }
    }


    @Override
    public HashSet<String> anagrams(String language, String word) {
        String vérification = dicoIE.checkLanguageAvailable(language);
        HashSet<String> possibleAnagrams = new HashSet<>();
        if (vérification.equals("")) {
            try {
                // c'est le code de l'exemple
                Dictionnary dico = dicoIE.load(language);
                String key = dico.normalize(word.trim());

                // on a extrait cette partie pour l'appeler depuis anagrammes( avec jockers) pour gagner les vérifications...
                // et le chargement du json
                lookingForAnagrams(key, dico, possibleAnagrams);


            } catch (IOException e) {
                return possibleAnagrams;
            }
        }

        return possibleAnagrams;
    }

    @Override
    public HashSet<String> anagrams(String language, String word, int nbJocker) {
        String vérification = dicoIE.checkLanguageAvailable(language);
        HashSet<String> possibleAnagrams = new HashSet<>();
        if (vérification.equals("")) {

            try {
                Dictionnary dico = dicoIE.load(language);
                String key = dico.normalize(word.trim());
                key = dico.anagram(key);

                String[] alphabet = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "j", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};

                for(int i = 0; i < alphabet.length; i++) {
                    String motexplore = key+alphabet[i];
                    if (nbJocker == 1) {
                        lookingForAnagrams(motexplore, dico, possibleAnagrams);

                    }
                    else if (nbJocker ==2) {
                        for(int j = 0; j < alphabet.length; j++) {
                            String motexplore2jockers = motexplore+alphabet[i];
                            lookingForAnagrams(motexplore2jockers, dico, possibleAnagrams);
                        }
                    }
                }
            } catch (IOException e) {
                return possibleAnagrams;
            }
        }

        return possibleAnagrams;
    }

    @Override
    public String oneWord(String language) {
        String vérification = dicoIE.checkLanguageAvailable(language);
        String mot = "";
        if (vérification.equals("")) {
            try {
                Dictionnary dico = dicoIE.load(language);
                Object[] keys = dico.getDictionnary().keySet().toArray();
                Object key = keys[rand.nextInt(keys.length)];
                ArrayList<String> liste = dico.getDictionnary().get(key);
                mot = liste.get(rand.nextInt(liste.size()));
            } catch (IOException e) {
                return mot;
            }
        }
        return mot;
    }

    @Override
    public String oneWord(String language, int size) {
        String vérification = dicoIE.checkLanguageAvailable(language);
        String mot = "";
        if (vérification.equals("")) {
            try {
                // c'est le code de l'exemple
                Dictionnary dico = dicoIE.load(language);
                Object[] keys = dico.getDictionnary().keySet().stream().filter(s -> s.length() == size).toArray();
                if (keys.length > 0) {
                    Object key = keys[rand.nextInt(keys.length)];
                    ArrayList<String> liste = dico.getDictionnary().get(key);
                    mot = liste.get(rand.nextInt(liste.size()));
                }

            } catch (IOException e) {
                return mot;
            }
        }
        return mot;
    }
}
