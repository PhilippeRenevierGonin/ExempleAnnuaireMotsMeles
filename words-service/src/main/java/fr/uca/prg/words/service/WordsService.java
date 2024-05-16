package fr.uca.prg.words.service;

import java.util.HashSet;

public interface WordsService {
    HashSet<String> anagrams(String language, String word);

    HashSet<String> anagrams(String language, String word, int nbJocker);

    String oneWord(String language);

    String oneWord(String language, int size);
}
