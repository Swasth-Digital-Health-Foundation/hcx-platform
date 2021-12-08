package com.oss.apigateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.oss.apigateway.constants.Constants;
import com.oss.apigateway.models.Acl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@SpringBootApplication
public class ApiGatewayApplication {

	@Autowired
	private ResourceLoader resourceLoader;

	@Bean("userKeyResolver")
	public KeyResolver userKeyResolver() {
		return exchange -> {
			if(exchange.getRequest().getHeaders().containsKey(Constants.X_JWT_SUB_HEADER)){
				String subject = exchange.getRequest().getHeaders().getFirst(Constants.X_JWT_SUB_HEADER);
				assert subject != null;
				return Mono.just(subject);
			}
			return Mono.just("1");
		};
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@SuppressWarnings("rawtypes, unchecked")
	@Bean
	public Map<String, Acl> aclMap(@Value("${rbac.path:classpath:rbac.yaml}") String filename) throws IOException {
		Resource resource = resourceLoader.getResource(filename);
		ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
		Map<String, Object> obj = yamlReader.readValue(resource.getInputStream(), Map.class);
		List<Map> rbacs = (List) obj.get("rbac");

		Map<String, Acl> aclMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		for(Map rbac : rbacs){
			Acl acl = new Acl();
			if(rbac.get("paths") != null){
				acl.setPaths(new HashSet((List) rbac.get("paths")));
			}
			if(rbac.get("regexPaths") != null){
				acl.setRegexPaths((List) rbac.get("regexPaths"));
			}
			aclMap.put((String) rbac.get("role"), acl);
		}

		return aclMap;

	}

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}
