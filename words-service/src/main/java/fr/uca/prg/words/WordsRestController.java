package fr.uca.prg.words;

import fr.uca.prg.verification.ServeurPasPret;
import fr.uca.prg.words.dico.Dictionnary;
import fr.uca.prg.words.service.DicoImportExport;
import fr.uca.prg.words.service.WordsService;
import fr.uca.prg.words.service.ie.Message;
import fr.uca.prg.words.verification.WordsServiceCheck;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;

@RestController
public class WordsRestController {

    @Autowired
    WordsServiceCheck wordsServiceCheck;

    @Autowired
    DicoImportExport dicoIE;

    @Autowired
    WordsService wordsService;

    public void finInit() throws UnknownHostException {
        wordsServiceCheck.verifierContrat();
    }

    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "retourne \"I'm alive\" "),
            @ApiResponse(responseCode = "503", description = "serveur pas prêt")
    })
    @GetMapping("/ping")
    public String ping() {
        if ( ! wordsServiceCheck.estPret())throw new ServeurPasPret();
        return "I'm alive";
    }

    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "liste des langues disponibles"),
            @ApiResponse(responseCode = "503", description = "serveur pas prêt")
    })
    @GetMapping("/langues")
    public ArrayList<String> listeDesLangues() {
        if ( ! wordsServiceCheck.estPret())throw new ServeurPasPret();

        return dicoIE.availableLanguages();
    }


    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "une chaine décrivant le résultat de l'ajout de la langue : un problème ou succès"),
    @ApiResponse(responseCode = "503", description = "serveur pas prêt")})
    @PostMapping("/ajouterLangue")
    public Mono<String> addLanguage(@RequestPart("fichierLangue") FilePart filePart, @RequestPart("langue") String language){
        if ( ! wordsServiceCheck.estPret())throw new ServeurPasPret();
        // note : FilePart pourrait aussi être un flux ou un mono (plutôt un mono, un flux voudrait dire plusieurs fichiers)

        Message feedback = dicoIE.checkFutureLanguage(language);
        if (feedback.isError()) return Mono.just(feedback.getDescription());

        // on récupère le fichier
        Mono<InputStream> monoInputStream = filePart.content().map(dataBuffer -> dataBuffer.asInputStream(true)).reduce(SequenceInputStream::new);
        return monoInputStream.flatMap(is -> {
            try {
                Dictionnary dico = dicoIE.load(is);
                is.close();
                dicoIE.export(dico, "langues/" + language + "/anagrammes.json");
            } catch (IOException e) {
                Mono.error(e);
            }
            return Mono.just("le fichier a été converti en json");
        });


    }



    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "la liste des anagrammes faits à partir du mot passé en path variable"),
    @ApiResponse(responseCode = "503", description = "serveur pas prêt")})
    @GetMapping("/{langue}/anagrammes/{mot}")
    public HashSet<String>  anagrams(@PathVariable("langue") String language, @PathVariable("mot") String word){
        if ( ! wordsServiceCheck.estPret())throw new ServeurPasPret();

        return wordsService.anagrams(language, word);
    }



    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "la liste des anagrammes faits à partir du mot passé en path variable avec une ou deux lettres en plus"),
    @ApiResponse(responseCode = "503", description = "serveur pas prêt")})
    @GetMapping("/{langue}/anagrammes/{mot}/jocker/{nb:[1|2]}")
    public HashSet<String>  annagrams(@PathVariable("langue") String language, @PathVariable("mot") String word,  @PathVariable("nb") int nb) {
        if ( ! wordsServiceCheck.estPret())throw new ServeurPasPret();

        return wordsService.anagrams(language, word, nb);
    }


    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "un mot au hasard de la langue passée en path variable"),
    @ApiResponse(responseCode = "503", description = "serveur pas prêt")})
    @GetMapping("/{langue}/unmot")
    public String oneWord(@PathVariable("langue") String language){
        if ( ! wordsServiceCheck.estPret())throw new ServeurPasPret();
        return wordsService.oneWord(language);
    }


    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "un mot au hasard de la langue passée en path variable de la taille spécifiée"),
    @ApiResponse(responseCode = "503", description = "serveur pas prêt")})
    @GetMapping("/{langue}/unmot/longueur/{taille:[1-9][0-9]*}")
    public String oneWord(@PathVariable("langue") String language, @PathVariable("taille") int size){
        if ( ! wordsServiceCheck.estPret())throw new ServeurPasPret();

        return wordsService.oneWord(language, size);
    }






}
