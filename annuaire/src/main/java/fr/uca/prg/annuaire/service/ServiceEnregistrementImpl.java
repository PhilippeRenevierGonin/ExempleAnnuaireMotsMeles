package fr.uca.prg.annuaire.service;

import fr.uca.prg.verification.Identification;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;

@Service
public class ServiceEnregistrementImpl implements ServiceEnregistrement {
    // clef = url du service, valeur = le contrat
    HashMap<String, OpenAPI> services = new HashMap<>();

    @Override
    public boolean enregistrerNouveauService(Identification nouveauService) {
        System.out.println("─────────────> enregistrerNouveauService "+nouveauService);
        boolean accepte = ! services.containsKey(nouveauService.getUrl());
        if (accepte) {
            services.put(nouveauService.getUrl(), nouveauService.getContrat());
        }
        return accepte;
    }

    @Override
    public String[] getListeDesServices() {
        System.out.println("─────────────> getListeDesServices ");

        return services.keySet().toArray(new String[services.size()]);
    }

    @Override
    public String[] rechercherAvecUnChemin(String chemin) {
        System.out.println("─────────────> rechercherAvecUnChemin "+chemin);

        ArrayList<String> urls = new ArrayList<>();

        services.forEach((url, contrat) -> {
            if (contrat.getPaths().containsKey(chemin)) urls.add(url);
        });

        return urls.toArray(new String[urls.size()]);
    }

    @Override
    public String[] rechercherAvecPlusieursChemins(String[] chemins) {
        System.out.println("─────────────> rechercherAvecPlusieursChemins ");

        ArrayList<String> urls = new ArrayList<>();

        services.forEach((url, contrat) -> {
            boolean ajout = true; int i = 0;
            while (ajout && i < chemins.length) {
                ajout = ajout && contrat.getPaths().containsKey(chemins[i]);
                i++;
            }
            if (ajout) urls.add(url);
        });


        String [] resultat = urls.toArray(new String[urls.size()]);
        return resultat;
    }

    @Override
    public boolean estEnregistre(String url) {
        System.out.println("─────────────> estEnregistre "+url);

        return services.containsKey(url);
    }

    @Override
    public void oublier(String url) {
        System.out.println("─────────────> oublier "+url);
        services.remove(url);
    }
}
