package fr.uca.prg.words.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import fr.uca.prg.words.dico.Dictionnary;
import fr.uca.prg.words.service.ie.Message;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;


@Service
public class DicoImportExportImpl implements DicoImportExport {

    private ObjectMapper mapper;

    public DicoImportExportImpl() {
        setMapper(new ObjectMapper());
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    /**
     * pour enregistrer le dictionnaire courant dans un fichier json
     * @param filename
     */
    @Override
    public void export(Dictionnary dictionnary, String filename) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), dictionnary);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * pour charger un fichier texte (un mot par ligne)
     * @param stream le flux du fichier texte
     * @throws IOException
     */
    @Override
    public Dictionnary load(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        Dictionnary dictionnaire = new Dictionnary();
        try {
            while (reader.ready()) {
                String line = reader.readLine();
                if (! dictionnaire.addWord(line)) System.out.println("mot rejeté : "+line);
            }
            reader.close();
        }
        catch (IOException e) {
            // pour mise au point // e.printStackTrace();
            dictionnaire = new Dictionnary();
            reader.close();
        }
        return dictionnaire;
    }

    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
        this.mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public ObjectMapper getMapper() {
        return mapper;
    }


    /**
     * vérifier "langues" puis le dossier "langues/<la langue>" puis l'existance du fichier anagrammes.json
     * @param language la langue
     * @return un message d'erreur ou "" s'il n'y a pas de soucis
     */
    @Override
    public String checkLanguageAvailable(String language) {
        File langDir = new File("langues");
        if (! langDir.exists()) {
            return "problème serveur : le dossier langues n'existe pas";
        }

        if (! langDir.isDirectory())  return "problème serveur : un dossier interne n'est pas un dossier";

        // on gère la langue
        langDir = new File("langues/"+language);
        if (langDir.exists()) {
            if (! langDir.isDirectory()) return "problème serveur : la langue spécifiée pose soucis (le dossier existe mais ce n'est pas un dossier)";
            File json = new File("langues/"+language+"/anagrammes.json");
            if (! json.exists()) return "la langue spécifiée pose soucis (lle fichier json n'existe pas)";
        }
        else {
            return "problème serveur : la langue spécifiée pose soucis (le dossier langue n'existe pas)";
        }

        return "";
    }

    @Override
    public Dictionnary load(String language) throws IOException {
        return mapper.readValue(new File("langues/" + language + "/anagrammes.json"), Dictionnary.class);
    }

    @Override
    public ArrayList<String> availableLanguages() {
        ArrayList<String> list = new ArrayList<>();
        File languages = new File("langues");
        if (languages.exists() && languages.isDirectory()) {
            File[] files = languages.listFiles();
            for(File f : files) {
                if (f.isDirectory()) {
                    File dico = new File(f.getPath()+"/anagrammes.json");
                    if (dico.exists()) list.add(f.getName());
                }
            }
        }
        return list;
    }

    @Override
    public Message checkFutureLanguage(String language) {
        // quelques vérifications sur le dossier "langues"
        File langDir = new File("langues");
        if (! langDir.exists()) {
            try {
                Files.createDirectories(Paths.get("langues/"));
            } catch (IOException e) {
                return new Message(true, "problème serveur : on ne peut pas créer le dossier langues");
            }
        }

        if (! langDir.isDirectory())  new Message(true,"problème serveur : un dossier interne n'est pas un dossier");

        // on gère la langue
        langDir = new File("langues/"+language);
        if (langDir.exists()) {
            if (! langDir.isDirectory()) return new Message(true,"problème serveur : la langue spécifiée pose soucis (le dossier existe mais ce n'est pas un dossier)");
            File json = new File("langues/"+language+"/anagrammes.json");
            if (json.exists()) return new Message(true,"la langue spécifiée pose soucis (le dossier et le fichier json existent déjà)");
        }
        else {
            try {
                Files.createDirectories(Paths.get("langues/"+language));
            } catch (IOException e) {
                return  new Message(true,Arrays.toString(e.getStackTrace()));
            }
        }

        return new Message(false, "prêt pour import");
    }
}
