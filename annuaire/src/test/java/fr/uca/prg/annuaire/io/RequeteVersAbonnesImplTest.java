package fr.uca.prg.annuaire.io;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RequeteVersAbonnesImplTest {

    MockWebServer [] mockWebServers; // pour simuler les appels rest vers l'extérieur


    @Autowired
    RequetesVersAbonnesImpl requeteVersAbonnes;

    @BeforeEach
    void setUp() {
        mockWebServers = new MockWebServer[3];
        try {
            for(int i = 0; i < mockWebServers.length; i++) {
                mockWebServers[i] = new MockWebServer();
                mockWebServers[i].start();
            }
        } catch (IOException e) {
            fail();
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        for(MockWebServer mockWebServer : mockWebServers) mockWebServer.close();
    }

    @Test
    void callBack() throws InterruptedException {
        for(MockWebServer mockWebServer : mockWebServers) {
            mockWebServer.enqueue(new MockResponse().setResponseCode(200));
        }


        Set<String> abonnes = new HashSet<>();
        String [] baseUrl =  new String[mockWebServers.length];
        for(int i = 0; i < mockWebServers.length; i++) {
            baseUrl[i] = "http://"+mockWebServers[i].getHostName()+":"+mockWebServers[i].getPort();
            abonnes.add(baseUrl[i]+"/service"+i+"/");
        }


        String service = "celui qui est eteint";

        requeteVersAbonnes.prevenirAbonne(service, abonnes);

        // on attend les 3 requetes -> boucle while avec timeout
        RecordedRequest [] requetes = new RecordedRequest[mockWebServers.length];
        int nbReq = 0;
        int nbTentative = 0;
        while (nbReq < mockWebServers.length) {
            for(int i = 0; i < mockWebServers.length; i++) {
                if (requetes[i] == null) {
                    requetes[i] = mockWebServers[i].takeRequest();
                    if (requetes[i] != null) nbReq++;
                }
            }
            nbTentative++;

            if (nbTentative > 200) fail();
            // on attend un peu
            TimeUnit.MILLISECONDS.sleep(50);
        }

        for(int i = 0; i < mockWebServers.length; i++) {
            RecordedRequest requete = requetes[i];
            assertEquals("POST", requete.getMethod());
            assertEquals("/service"+i+"/serviceeteint", requete.getPath());
            assertEquals(baseUrl[i]+"/service"+i+"/serviceeteint", requete.getRequestUrl().url().toString());
            assertEquals(service, requete.getBody().readString(Charset.defaultCharset()));
        }


        // facultatif : on verifie qu'il n'y a pas d'autre requete (on attend
        for(MockWebServer mockWebServer : mockWebServers)  {
            RecordedRequest requete = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
            assertNull(requete);
        }

    }
}