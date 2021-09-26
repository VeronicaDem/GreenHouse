package ru.hack.greenmarchrootes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import ru.hack.greenmarchrootes.logistics.Util;

@SpringBootApplication
public class GreenMarchrootesApplication {

	public static void main(String[] args) {
		SpringApplication.run(GreenMarchrootesApplication.class, args);
		Util.initializeStandards();
		Util.initializeStreets();
	}

}
