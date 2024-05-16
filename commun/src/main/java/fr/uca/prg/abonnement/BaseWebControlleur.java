package fr.uca.prg.abonnement;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public abstract class BaseWebControlleur {

    @PostMapping("/serviceeteint")
    public void serviceEteint(@RequestBody String url) {
        serviceQuiDisparait(url);
    }

    protected abstract void serviceQuiDisparait(String url) ;
}
