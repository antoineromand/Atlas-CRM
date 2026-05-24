package com.antoineromand.atlascrm;

import org.springframework.boot.SpringApplication;

public class TestAtlascrmApplication {

	public static void main(String[] args) {
		SpringApplication.from(AtlascrmApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
