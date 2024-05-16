package fr.uca.prg.abonnement;

import java.util.Objects;

public class Abonnement {
    private String id;
    private String service;

    public Abonnement() {

    }

    public Abonnement(String id, String service) {
        setId(id);
        setService(service);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Abonnement that = (Abonnement) o;
        return Objects.equals(id, that.id) && Objects.equals(service, that.service);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, service);
    }
}
