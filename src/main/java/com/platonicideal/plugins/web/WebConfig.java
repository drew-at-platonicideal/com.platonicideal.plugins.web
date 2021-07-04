package com.platonicideal.plugins.web;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

	@Bean
	public Reflections piScanner() {
		return new Reflections(new ConfigurationBuilder()
				.setUrls(ClasspathHelper.forPackage("com.platonicideal"))
				.setScanners(new MethodAnnotationsScanner(),
						     new TypeAnnotationsScanner())
				.filterInputsBy(new FilterBuilder().includePackage("com.platonicideal")));
	}
	
}
