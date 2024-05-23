package fr.uca.prg.annuaire;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uca.prg.abonnement.Abonnement;
import fr.uca.prg.annuaire.io.AnnuaireControlleurWeb;
import fr.uca.prg.annuaire.io.RequetesVersAbonnes;
import fr.uca.prg.annuaire.service.ServiceAbonnnement;
import fr.uca.prg.annuaire.service.ServiceEnregistrement;
import fr.uca.prg.verification.Identification;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,   properties = {
        "server.port=8024"
})class ScenarioAvecArretWSITCase {

    @SpyBean
    RequetesVersAbonnes requetesVersAbonnes;

    @SpyBean
    ServiceAbonnnement serviceAbonnnement;

    @SpyBean
    ServiceEnregistrement serviceEnregistrement;

    @SpyBean
    AnnuaireControlleurWeb restCtrl;

    @Autowired
    private ObjectMapper objectMapper;

    Identification idS1;
    Identification idS2;


    final String ip1 = "172.42.0.243";
    final String ip2 = "172.42.0.245";


    Runtime runtime = Runtime.getRuntime();
    final String stopWs = "docker stop  -t 0 ws-ip-172.42.0.243";
    final String stopGg = "docker stop  -t 0 gg-ip-172.42.0.245";

    final String startWs = "docker start ws-ip-172.42.0.243";
    final String startGg = "docker start gg-ip-172.42.0.245";

    @BeforeEach
    void setUp() {
        arreterContainerDocker();


        try {
            OpenAPI contrat1 = objectMapper.readValue(getClass().getResource("/ws-openapi-243.json"), OpenAPI.class);
            idS1 = new Identification("http://" + ip1 + ":8180/", contrat1);

            OpenAPI contrat2 = objectMapper.readValue(getClass().getResource("/gg-openapi-245.json"), OpenAPI.class);
            idS2 = new Identification("http://" + ip2 + ":8280/", contrat2);
        } catch (IOException e) {
            fail();
        }


        try {
            runtime.exec(startWs);
            runtime.exec(startGg);
        } catch (IOException e) {
            fail();
        }

    }

    @AfterEach
    void arreterContainerDocker() {
        try {
            runtime.exec(stopWs);
            runtime.exec(stopGg);
        } catch (IOException e) {
            fail();
        }
    }


    @Test
    void scenarioAbonnementPuisDesabonnement() throws Exception {

        Object synchro = new Object();
        Abonnement abonnement = new Abonnement(idS2.getUrl(), idS1.getUrl());


        doAnswer(invocationOnMock -> {
            invocationOnMock.callRealMethod();
            synchronized (synchro) {
                synchro.notify();
            }
            return null;
        }).when(serviceAbonnnement).traiterDemandeAbonnement(abonnement);

        synchronized (synchro) {
            synchro.wait(59000); // il y a 30 secondes entre 2 tentatives des services on laisse le temps pour lancer dans le bon ordre
        }

        verify(restCtrl, times(1)).enregistrement(idS1);
        verify(serviceEnregistrement, times(1)).enregistrerNouveauService(idS1);

        verify(restCtrl, times(1)).enregistrement(idS2);
        verify(serviceEnregistrement, times(1)).enregistrerNouveauService(idS2);

        String[] cheminsRecherches = {"/langues", "/{langue}/unmot/longueur/{taille}"};

        verify(restCtrl, atLeast(1)).rechercherMultiplesChemins(cheminsRecherches);
        verify(serviceEnregistrement, atLeast(1)).rechercherAvecPlusieursChemins(cheminsRecherches);

        verify(restCtrl, times(1)).sAbonner(abonnement);
        verify(serviceAbonnnement, times(1)).traiterDemandeAbonnement(abonnement);

        verify(serviceEnregistrement, times(1)).estEnregistre(idS1.getUrl());
        verify(serviceEnregistrement, times(1)).estEnregistre(idS2.getUrl());

        clearInvocations(restCtrl);
        clearInvocations(serviceEnregistrement);
        clearInvocations(serviceAbonnnement);
        clearInvocations(requetesVersAbonnes);

        // il faut laisser le temps à gg-ip-172.42.0.245 le temps de faire sa grille.
        // solution 1 : il faudrait savoir quand il peut le faire, en interrogant le service (par exemple, en lui demandant la génération d'une grille)
        // solution 2, alétoire : on attend 10s...
        TimeUnit.SECONDS.sleep(10);

        // on prépare les verify
        HashSet<String> leSeulAbonne = new HashSet<>();
        leSeulAbonne.add(idS2.getUrl());
        String urlEncodee = URLEncoder.encode(idS1.getUrl(), StandardCharsets.UTF_8.toString());

        // le container docker n'est pas directement accessible, pour cela il faudrait une configuration réseau
        // ici pour simplifier, on transforme l'ip en localhost car il y a un binding de port
        HashSet<String> abonneLocalhost = new HashSet<>();
        abonneLocalhost.add(idS2.getUrl().replace(ip2, "localhost"));
        doAnswer(invocationOnMock -> {
            // on change le paramètre...
            requetesVersAbonnes.prevenirAbonne(idS1.getUrl(), abonneLocalhost);
            return null;
        }).when(requetesVersAbonnes).prevenirAbonne(idS1.getUrl(), leSeulAbonne);


        // on prepare la synchro
        Object deuxiemeSynchro = new Object();
        final int[] nbRecherches = new int[1];
        nbRecherches[0] = 0;
        doAnswer(invocationOnMock -> {
            String [] retour = (String[]) invocationOnMock.callRealMethod();
            assertEquals(0, retour.length);
            nbRecherches[0]++;
            System.out.println("----> on a appelé rechercherAvecPlusieursChemins "+nbRecherches[0]);
            // on attend la 10e fois pour reveiller le test
            if (nbRecherches[0] >= 10) {
                System.out.println("----> réveil du test");
                synchronized (deuxiemeSynchro) {
                    deuxiemeSynchro.notify();
                }
            }

            return retour;
        }).when(serviceEnregistrement).rechercherAvecPlusieursChemins(cheminsRecherches);

        //
        try {
            String stopWS = "docker exec ws-ip-172.42.0.243 bash -c \"/stopjava.sh\"";
            runtime.exec(stopWS);
        } catch (IOException e) {
            fail();
        }

        synchronized (deuxiemeSynchro) {
            deuxiemeSynchro.wait(59000);
        }


        verify(restCtrl, times(1)).dereferrencement(urlEncodee);
        verify(serviceAbonnnement, times(1)).traiterArretService(idS1.getUrl());
        verify(serviceEnregistrement, times(1)).estEnregistre(idS1.getUrl());
        verify(requetesVersAbonnes, times(1)).prevenirAbonne(idS1.getUrl(), leSeulAbonne);

        verify(restCtrl, times(10)).rechercherMultiplesChemins(cheminsRecherches);
        verify(serviceEnregistrement, times(10)).rechercherAvecPlusieursChemins(cheminsRecherches);

    }


}