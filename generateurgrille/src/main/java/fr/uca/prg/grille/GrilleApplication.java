package fr.uca.prg.grille;

import fr.uca.prg.grille.io.GrilleRestController;
import fr.uca.prg.grille.verification.VerificateurGrille;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GrilleApplication {


	public static void main(String[] args) {
		SpringApplication.run(GrilleApplication.class, args);
	}

	/*
	 * attention : "autowired" implicit
	 */
	@Bean
	public CommandLineRunner commandLineRunner(VerificateurGrille verificateur, GrilleRestController restController) {
		return args -> {
			if (args.length == 1) {
				verificateur.setUrlAnnuaire(args[0]);
			}
			restController.finInit();
		};
	}

}
