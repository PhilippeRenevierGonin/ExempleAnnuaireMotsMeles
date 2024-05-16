package fr.uca.prg.verification;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ServeurPasPret extends RuntimeException {
    public ServeurPasPret() {
        super("serveur pas encore pret");
    }
}
