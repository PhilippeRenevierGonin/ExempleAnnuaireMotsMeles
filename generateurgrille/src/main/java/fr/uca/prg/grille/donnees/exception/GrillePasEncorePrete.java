package fr.uca.prg.grille.donnees.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PARTIAL_CONTENT)
public class GrillePasEncorePrete extends RuntimeException {
}
