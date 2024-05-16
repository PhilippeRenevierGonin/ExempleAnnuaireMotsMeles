package fr.uca.prg.annuaire.service;

import fr.uca.prg.verification.Identification;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest class ServiceEnregistrementImplTest {

    @Autowired
    ServiceEnregistrementImpl serviceEnreg;

    @Mock
    OpenAPI contrat;




    @AfterEach
    void razAnnuaire() {
        // pas obligatoire ici, mais c'est un bon réflexe
        serviceEnreg.services = new HashMap<>();
    }

    // retirer un service inconnu : il ne se passe rien...
    @Test
    void retirerServiceInconnu() {
        String urlService = "une url connue";
        Identification id = new Identification(urlService, contrat);
        assertTrue(serviceEnreg.enregistrerNouveauService(id));

        assertEquals(1, serviceEnreg.services.size());
        serviceEnreg.oublier("un url quelconque inconnue");
        assertEquals(1, serviceEnreg.services.size());
    }

    @Test
    void retirerServiceConnu() {
        String urlService = "une url connue";
        Identification id = new Identification(urlService, contrat);
        serviceEnreg.enregistrerNouveauService(id);

        assertEquals(1, serviceEnreg.services.size());
        serviceEnreg.oublier(urlService);
        assertEquals(0, serviceEnreg.services.size());
    }



}