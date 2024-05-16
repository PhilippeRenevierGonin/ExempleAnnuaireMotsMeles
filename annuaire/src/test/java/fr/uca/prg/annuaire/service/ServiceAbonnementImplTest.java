package fr.uca.prg.annuaire.service;

import fr.uca.prg.abonnement.Abonnement;
import fr.uca.prg.annuaire.io.RequetesVersAbonnes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashMap;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * différents cas de tests sur traiterArretService
 */
@SpringBootTest class ServiceAbonnementImplTest {
    @Autowired
    ServiceAbonnnementImpl serviceAbo;

    @MockBean
    ServiceEnregistrement serviceEnreg;

    @MockBean
    RequetesVersAbonnes requeteVersAbonnes;


    @AfterEach
    void razAnnuaire() {
        serviceAbo.abonnements = new HashMap<>();
    }

    @Test
    void retirerServiceInconnu() {
        // pour montrer que le composant est le même sur tous les tests
        // assertEquals(0, serviceAbo.abonnements.size()); // il reste une liste vide

        when(serviceEnreg.estEnregistre(anyString())).thenReturn(false);
        assertFalse(serviceAbo.traiterArretService("une url inconnue"));
        verify(requeteVersAbonnes, never()).prevenirAbonne(anyString(), any());
    }

    @Test
    void retirerServiceConnu() {
        when(serviceEnreg.estEnregistre("une url")).thenReturn(true);

        assertTrue(serviceAbo.traiterArretService("une url"));
        verify(requeteVersAbonnes, never()).prevenirAbonne(anyString(), any());
        verify(serviceEnreg, times(1)).estEnregistre("une url");

        // il n'y avait pas d'abonnement, il n'y en a toujours pas
        assertEquals(0, serviceAbo.abonnements.size());
    }


    @Test
    void retirerServiceConnuAvec1Abonne() {
        Abonnement abo = new Abonnement("une url", "une autre url");
        when(serviceEnreg.estEnregistre("une url")).thenReturn(true);
        when(serviceEnreg.estEnregistre("une autre url")).thenReturn(true);

        serviceAbo.traiterDemandeAbonnement(abo);
        assertEquals(1, serviceAbo.abonnements.size());

        assertTrue(serviceAbo.traiterArretService(abo.getService()));
        HashSet<String> param = new HashSet<>();
        param.add(abo.getId());
        verify(requeteVersAbonnes, times(1)).prevenirAbonne(abo.getService(), param);

        // au total
        verify(serviceEnreg, times(2)).estEnregistre("une autre url");
        verify(serviceEnreg, times(1)).estEnregistre("une url");

        // on a retire l'abo au service qu'on enleve
        assertEquals(0, serviceAbo.abonnements.size());
    }


    @Test
    void retirerServiceConnuAvecAbonnement() {
        Abonnement abo = new Abonnement("une url", "une autre url");
        when(serviceEnreg.estEnregistre("une url")).thenReturn(true);
        when(serviceEnreg.estEnregistre("une autre url")).thenReturn(true);

        serviceAbo.traiterDemandeAbonnement(abo);
        assertEquals(1, serviceAbo.abonnements.size());

        assertTrue(serviceAbo.traiterArretService("une url"));
        verify(requeteVersAbonnes, never()).prevenirAbonne(anyString(), any());

        // au total
        verify(serviceEnreg, times(1)).estEnregistre("une autre url");
        verify(serviceEnreg, times(2)).estEnregistre("une url");

        // on a retire l'abo du service qu'on enleve
        assertEquals(1, serviceAbo.abonnements.size()); // il reste une liste vide
        assertEquals(0, serviceAbo.abonnements.get(abo.getService()).size());
    }


    @Test
    void retirerServiceConnuAvecAbonneAvecAbonnement() {
        Abonnement aboSurRetire = new Abonnement("une url", "une autre url");
        Abonnement aboDuRetire = new Abonnement(aboSurRetire.getService(), "encore une autre url");

        when(serviceEnreg.estEnregistre(aboSurRetire.getId())).thenReturn(true); // "une url"
        when(serviceEnreg.estEnregistre(aboSurRetire.getService())).thenReturn(true); // "une autre url"
        when(serviceEnreg.estEnregistre(aboDuRetire.getService())).thenReturn(true); // "encore une autre url"

        serviceAbo.traiterDemandeAbonnement(aboSurRetire);
        serviceAbo.traiterDemandeAbonnement(aboDuRetire);
        assertEquals(2, serviceAbo.abonnements.size());

        assertTrue(serviceAbo.traiterArretService(aboSurRetire.getService()));
        HashSet<String> param = new HashSet<>();
        param.add(aboSurRetire.getId());
        verify(requeteVersAbonnes, times(1)).prevenirAbonne(aboSurRetire.getService(), param);

        // au total
        verify(serviceEnreg, times(3)).estEnregistre(aboSurRetire.getService());
        verify(serviceEnreg, times(1)).estEnregistre(aboSurRetire.getId());
        verify(serviceEnreg, times(1)).estEnregistre(aboDuRetire.getService());

        // on a retire aboSurRetire et il reste une liste vide
        assertEquals(1, serviceAbo.abonnements.size()); // il reste une liste vide
        assertEquals(0, serviceAbo.abonnements.get(aboDuRetire.getService()).size());
    }

}