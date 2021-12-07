package tech.getarrays.supportportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.File;

import static tech.getarrays.supportportal.constant.FileConstant.USER_FOLDER;


@ComponentScan("UserRepository")
@SpringBootApplication
public class SupportportalApplication {

	public static void main(String[] args) {
		SpringApplication.run(SupportportalApplication.class, args);
		new File(USER_FOLDER).mkdirs();


	}
	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder(){
		return new BCryptPasswordEncoder();
	}
}
