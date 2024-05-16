package fr.uca.prg.annuaire.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uca.prg.abonnement.Abonnement;
import fr.uca.prg.annuaire.service.ServiceAbonnnement;
import fr.uca.prg.annuaire.service.ServiceEnregistrement;
import fr.uca.prg.verification.Identification;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AnnuaireControlleurWebTest {
    @MockBean
    ServiceEnregistrement serviceEnregistrement;

    @MockBean
    ServiceAbonnnement serviceAbonnnement;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    OpenAPI contrat = new OpenAPI();
    Identification id = new Identification("http://service.com/", contrat);

    @Test
    void enregistrement() throws Exception {
        String paramEnreg = objectMapper.writeValueAsString(id);

        when(serviceEnregistrement.enregistrerNouveauService(id)).thenReturn(true);

        mockMvc.perform( post("/senregistrer").contentType(MediaType.APPLICATION_JSON).content(paramEnreg))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("true")));

        verify(serviceEnregistrement, times(1)).enregistrerNouveauService(id);
    }

    @Test
    void sAbonner() throws Exception {
        Abonnement abonnement = new Abonnement("ecouteur", "ecoute");


        mockMvc.perform( post("/sabonner").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(abonnement)))
                .andExpect(status().isOk());


        verify(serviceAbonnnement, times(1)).traiterDemandeAbonnement(abonnement);
    }

    @Test
    void dereferrencement() throws Exception {

        when(serviceAbonnnement.traiterArretService(id.getUrl())).thenReturn(true);


        String url = URLEncoder.encode(id.getUrl(), StandardCharsets.UTF_8.toString());
        mockMvc.perform( delete("/seffacer/"+url))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("true")));

        verify(serviceAbonnnement, times(1)).traiterArretService(id.getUrl());

    }

    @Test
    void rechercherMultiplesChemins() throws Exception {
        String[] cheminsVoulus = {"/langues", "/{langue}/unmot/longueur/{taille}"};
        String[] retours = {"service1", "service2", "service3"};

        when(serviceEnregistrement.rechercherAvecPlusieursChemins(cheminsVoulus)).thenReturn(retours);
        mockMvc.perform( get("/rechercher/multiples").param("chemins", cheminsVoulus)   )
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(retours)));

        verify(serviceEnregistrement, times(1)).rechercherAvecPlusieursChemins(cheminsVoulus);
    }
}