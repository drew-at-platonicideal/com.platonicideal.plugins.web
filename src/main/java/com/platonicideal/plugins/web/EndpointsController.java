package com.platonicideal.plugins.web;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.util.ListUtils;

@Controller
public class EndpointsController {

	private final Reflections endpointScanner;

	@Autowired
	public EndpointsController(Reflections endpointScanner) {
		this.endpointScanner = endpointScanner;
	}
	
	@RequestMapping(value = "/endpoints", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE })
    @ResponseBody
    public List<EndpointDescription> getEndpoints(@RequestParam(required = false, defaultValue = "") String containing) {
		Set<Method> methods = endpointScanner.getMethodsAnnotatedWith(GetMapping.class);
		List<EndpointDescription> endpoints = methods.stream()
				.map(m -> new EndpointDescription(m.getDeclaredAnnotation(GetMapping.class).value()[0], ""))
				.filter((d) -> d.contains(containing))
				.collect(Collectors.toList());
		return ListUtils.sort(endpoints);
    }
	
	private static class EndpointDescription implements Comparable<EndpointDescription> {
		public final String url;
		public final String description;
		
		public EndpointDescription(String url, String description) {
			this.url = url;
			this.description = description;
		}

		public boolean contains(String v) {
			return this.url.contains(v) || this.description.contains(v);
		}
		
		@Override
		public int compareTo(EndpointDescription o) {
			if(this.description.compareTo(o.description) != 0) {
				return this.description.compareTo(o.description);
			}
			return this.url.compareTo(o.url);
		}
	}
	
}
