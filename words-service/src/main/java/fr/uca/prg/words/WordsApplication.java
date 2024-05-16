package fr.uca.prg.words;

import fr.uca.prg.words.verification.WordsServiceCheck;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WordsApplication {

	public static void main(String[] args) {
		SpringApplication.run(WordsApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(WordsServiceCheck verificateur, WordsRestController restController) {
		return args -> {
			if (args.length == 1) {
				verificateur.setUrlAnnuaire(args[0]);
			}
			restController.finInit();
		};
	}

}
