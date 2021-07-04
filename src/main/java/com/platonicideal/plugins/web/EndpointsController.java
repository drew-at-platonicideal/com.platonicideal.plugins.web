package com.platonicideal.plugins.web;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

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
		List<EndpointDescription> endpoints = 
				getClasses("com.platonicideal").stream()
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
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			URI uri = new URI(resource.toString());
			dirs.add(new File(uri.getPath()));
		}
		List<Class<?>> classes = new ArrayList<>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}

		return classes;
	}

	private List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class<?>> classes = new ArrayList<>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;
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
