package fr.uca.prg.annuaire;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uca.prg.abonnement.Abonnement;
import fr.uca.prg.annuaire.io.AnnuaireControlleurWeb;
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

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,   properties = {
        "server.port=8025"
})
class ScenarioITCase {



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


    final String ip1 = "172.42.0.233";
    final String ip2 = "172.42.0.235";


    Runtime runtime = Runtime.getRuntime();
    final String stopWs = "docker stop  -t 0 ws-ip-172.42.0.233";
    final String stopGg = "docker stop  -t 0 gg-ip-172.42.0.235";

    final String startWs = "docker start ws-ip-172.42.0.233";
    final String startGg = "docker start gg-ip-172.42.0.235";

    @BeforeEach
    void setUp() {
        arreterContainerDocker();


        try {
            OpenAPI contrat1 = objectMapper.readValue(getClass().getResource("/ws-openapi-233.json"), OpenAPI.class);
            idS1 = new Identification("http://" + ip1 + ":8181/", contrat1);

            OpenAPI contrat2 = objectMapper.readValue(getClass().getResource("/gg-openapi-235.json"), OpenAPI.class);
            idS2 = new Identification("http://" + ip2 + ":8281/", contrat2);
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
    void scenarioAbonnement() throws Exception {

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
    }



}