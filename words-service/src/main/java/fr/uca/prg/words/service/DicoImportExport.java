package fr.uca.prg.words.service;

import fr.uca.prg.words.dico.Dictionnary;
import fr.uca.prg.words.service.ie.Message;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public interface DicoImportExport {
    void export(Dictionnary dictionnary, String filename);

    Dictionnary load(InputStream stream) throws IOException;

    String checkLanguageAvailable(String language);

    Dictionnary load(String language) throws IOException;

    ArrayList<String> availableLanguages();

    Message checkFutureLanguage(String language);
}
