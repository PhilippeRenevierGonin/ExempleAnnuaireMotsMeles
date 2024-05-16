package fr.uca.prg.words.verification;


import fr.uca.prg.verification.Verificateur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class WordsServiceCheck extends Verificateur {
	
	// pour récupérer le numéro de port, mais ce n'est pas immédiat
    @Autowired
    Environment environment;

    @Override
    public Environment getEnvironment() {
        return environment;
    }

	// pour savoir si le service est prêt
    public boolean estPret() {
        return isContratPret() ;
    }



}
