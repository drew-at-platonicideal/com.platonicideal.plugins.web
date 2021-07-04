package com.platonicideal.plugins.web;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EndpointsController {

	@RequestMapping(value = "/endpoints", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public List<EndpointDescription> getEndpoints(
			@RequestParam(required = false, defaultValue = "") String containing) throws ClassNotFoundException, IOException, URISyntaxException {
		Collection<Class<?>> klazzes = getClasses("com.platonicideal");
		System.out.println(klazzes);
		List<EndpointDescription> endpoints = 
				klazzes.stream()
					.flatMap(klazz -> Arrays.asList(klazz.getMethods()).stream())
					.map(m -> m.getAnnotation(GetMapping.class))
					.filter(an -> an != null)
					.filter(an -> an.value()[0].contains(containing))
					.map(EndpointDescription::describedBy)
					.collect(Collectors.toList());
		Collections.sort(endpoints);
		return endpoints;
	}

	private Collection<Class<?>> getClasses(String packageName) throws ClassNotFoundException, IOException, URISyntaxException {
		ClassPathScanningCandidateComponentProvider provider =
			    new ClassPathScanningCandidateComponentProvider(true);
		Set<Class<?>> klazzes = new HashSet<>();
		String basePackage = packageName.replace(".", "/");
		Set<BeanDefinition> beanDefinitions = provider.findCandidateComponents(basePackage);
		for (BeanDefinition beanDefinition : beanDefinitions) {
			String klazzName = beanDefinition.getBeanClassName();
			klazzes.add((Class<?>) Class.forName(klazzName));
		}
		return klazzes;
	}

	private static class EndpointDescription implements Comparable<EndpointDescription> {
		public final String url;
		public final String name;

		public static EndpointDescription describedBy(GetMapping m) {
			String url = m.value()[0];
			String name = m.name();
			return new EndpointDescription(url, name != null ? name : "");
		}
		
		private EndpointDescription(String url, String name) {
			this.url = url;
			this.name = name;
		}

		@Override
		public int compareTo(EndpointDescription o) {
			if (this.name.compareTo(o.name) != 0) {
				return this.name.compareTo(o.name);
			}
			return this.url.compareTo(o.url);
		}
	}

}
