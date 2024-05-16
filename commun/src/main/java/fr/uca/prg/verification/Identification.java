package fr.uca.prg.verification;

import io.swagger.v3.oas.models.OpenAPI;

import java.util.Objects;

public class Identification {

    String url ="";
    OpenAPI contrat = null;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public OpenAPI getContrat() {
        return contrat;
    }

    public void setContrat(OpenAPI contrat) {
        this.contrat = contrat;
    }

    public Identification() {

    }

    public Identification(String url, OpenAPI contrat) {
        setContrat(contrat);
        setUrl(url);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identification that = (Identification) o;
        return Objects.equals(url, that.url) && Objects.equals(contrat, that.contrat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, contrat);
    }
}
