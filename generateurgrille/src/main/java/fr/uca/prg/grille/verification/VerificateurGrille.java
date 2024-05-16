package fr.uca.prg.grille.verification;


import fr.uca.prg.grille.io.RequetesMots;
import fr.uca.prg.verification.Verificateur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
public class VerificateurGrille extends Verificateur {
	
	// pour récupérer le numéro de port, mais ce n'est pas immédiat
    @Autowired
    Environment environment;

    @Autowired
    RequetesMots requetesMots;

    String urlWS = "";

    public String getUrlWordsService() {
        return urlWS;
    }

    public void setUrlWordsService(String urlWS) {
        this.urlWS = urlWS;
    }

    private boolean frDispo = false;


	// pour savoir si le service est prêt
    public boolean estPret() {
        return isContratPret() && frDispo ;
    }

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    public void verifierLangue() {
        System.out.println("************************** code de recherche de WS ***********************");
        verifierLangueRecursif( 10);
    }

    private void verifierLangueRecursif(int nbTentatives) {
        if (nbTentatives == 0) {
            System.out.println("------> échec définitif à trouver un WS");
        }
        else {
            String[] cheminsVoulus = {"/langues", "/{langue}/unmot/longueur/{taille}"};
            String[] pathVariables = {"{langue}", "{taille}"};

            rechercheService(cheminsVoulus, pathVariables).subscribe(services -> {
                System.out.println("------> services = "+Arrays.toString(services));
                Thread t = new Thread( () -> {
                        for(int i = 0; i < services.length; i++) {
                            requetesMots.setUrl(services[i]);
                            String languesDispo[] = requetesMots.languesDiponibles();
                            boolean fr = Arrays.stream(languesDispo).anyMatch("fr"::equals);
                            if (fr) {
                                System.out.println("------> "+services[i] + " contient fr, on arrête <------");
                                frDispo = true;
                                setUrlWordsService(services[i]);
                                sAbonner(services[i]);
                                break;
                            }
                            else {
                                System.out.println("------> "+services[i] + " ne contient pas fr, on continue");
                            }
                        }
                        prevenirEcouteurs();
                        if (! frDispo) {
                            System.out.println("------> échec : "+nbTentatives);
                            try {
                                TimeUnit.SECONDS.sleep(2);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            verifierLangueRecursif(nbTentatives-1);
                        }

                });
                t.start();
            });
        }

    }


    public void reInitialiseLangue() {
        frDispo = false;
        setUrlWordsService("");
        verifierLangue();
    }
}
