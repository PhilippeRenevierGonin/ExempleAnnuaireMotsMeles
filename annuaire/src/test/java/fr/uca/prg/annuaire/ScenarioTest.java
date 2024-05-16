package fr.uca.prg.annuaire;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uca.prg.abonnement.Abonnement;
import fr.uca.prg.annuaire.io.AnnuaireControlleurWeb;
import fr.uca.prg.annuaire.io.RequetesVersAbonnes;
import fr.uca.prg.annuaire.service.ServiceAbonnnement;
import fr.uca.prg.annuaire.service.ServiceEnregistrement;
import fr.uca.prg.verification.Identification;
import io.swagger.v3.oas.models.OpenAPI;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ScenarioTest {

    @SpyBean
    RequetesVersAbonnes requeteVersAbonnes;

    @SpyBean
    ServiceEnregistrement serviceEnregistrement;

    @SpyBean
    ServiceAbonnnement serviceAbonnnement;

    @SpyBean
    AnnuaireControlleurWeb restCtrl;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    OpenAPI contrat = new OpenAPI();
    Identification idS1 = new Identification("http://words.com/", contrat);
    Identification idS2;
    MockWebServer mockWebServer;

    @BeforeEach
    void setUp() {
        mockWebServer = new MockWebServer();
        idS2 = new Identification("http://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort(), contrat);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.close();
    }

    @Test
    void scenario() throws Exception {

        mockMvc.perform(post("/senregistrer").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(idS1)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("true")));

        // on pourrait aussi faire des verify sur les méthodes du RestController
        verify(restCtrl, times(1)).enregistrement(idS1);
        verify(serviceEnregistrement, times(1)).enregistrerNouveauService(idS1);


        mockMvc.perform(post("/senregistrer").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(idS2)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("true")));

        verify(restCtrl, times(1)).enregistrement(idS2);
        verify(serviceEnregistrement, times(1)).enregistrerNouveauService(idS2);


        Abonnement abonnement = new Abonnement(idS2.getUrl(), idS1.getUrl());


        mockMvc.perform(post("/sabonner").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(abonnement)))
                .andExpect(status().isOk());

        verify(restCtrl, times(1)).sAbonner(abonnement);
        verify(serviceAbonnnement, times(1)).traiterDemandeAbonnement(abonnement);
        verify(serviceEnregistrement, times(1)).estEnregistre(idS1.getUrl());
        verify(serviceEnregistrement, times(1)).estEnregistre(idS2.getUrl());

        mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        String url = URLEncoder.encode(idS1.getUrl(), StandardCharsets.UTF_8.toString());
        mockMvc.perform(delete("/seffacer/" + url))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("true")));

        Set<String> abonnes = new HashSet<>();
        abonnes.add(idS2.getUrl());
        verify(restCtrl, times(1)).dereferrencement(url);
        verify(serviceAbonnnement, times(1)).traiterArretService(idS1.getUrl());
        // verify sur serviceEnregistrement
        // les comptes se cumulent : c'est le 2e appel à estEnregistre pour idS1
        verify(serviceEnregistrement, times(2)).estEnregistre(idS1.getUrl());
        verify(serviceEnregistrement, times(1)).oublier(idS1.getUrl());
        verify(requeteVersAbonnes, times(1)).prevenirAbonne(idS1.getUrl(), abonnes);

        // on attend que la requête soit faite
        RecordedRequest requete = null;
        int nbTentative = 0;
        while (requete == null) {
            requete = mockWebServer.takeRequest();
            nbTentative++;
            if (nbTentative > 200) fail();
            // on attend un peu
            TimeUnit.MILLISECONDS.sleep(50);
        }

        assertEquals("POST", requete.getMethod());
        assertEquals("/serviceeteint", requete.getPath());
        assertEquals(idS2.getUrl()+"/serviceeteint", requete.getRequestUrl().url().toString());
        assertEquals(idS1.getUrl(), requete.getBody().readString(Charset.defaultCharset()));

    }


}