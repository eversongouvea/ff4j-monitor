package org.ff4j.springboot2.config;

import org.ff4j.FF4j;
import org.ff4j.core.Feature;
import org.ff4j.property.PropertyInt;
import org.ff4j.property.PropertyString;
import org.ff4j.springboot2.consul.ConsulConnection;
import org.ff4j.springboot2.consul.store.FeatureStoreConsul;
import org.ff4j.springboot2.consul.store.PropertyStoreConsul;
import org.ff4j.strategy.el.ExpressionFlipStrategy;
import org.ff4j.utils.Util;
import org.ff4j.web.ApiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.orbitz.consul.Consul;

/**
 * FF4J configuration
 *
 * @author <a href="mailto:drizztguen77@gmail.com">Curtis White</a>
 */
@Configuration
public class FF4JConfiguration {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${server.port:8080}")
	private Integer serverPort;

	@Value("${ff4j.webapi.authentication:false}")
	private boolean authentication;

	@Value("${ff4j.webapi.authorization:false}")
	private boolean authorization;

	@Bean
	@ConditionalOnMissingBean
	public FF4j getFF4j() {

		FF4j ff4j = new FF4j().audit(true).autoCreate(true);

		Consul c = Consul.builder().withUrl("http://localhost:8500").build();
		ConsulConnection connection = new ConsulConnection(c);
		FeatureStoreConsul featureStoreConsul = new FeatureStoreConsul(connection);
		
		ff4j.setFeatureStore(featureStoreConsul);
		ff4j.setPropertiesStore(new PropertyStoreConsul(connection));		
		ff4j.createSchema();
		
		/*
		ff4j.createFeature("f1").createFeature("AwesomeFeature").createFeature("f2").createFeature("f3")
				.createProperty(new PropertyString("SampleProperty", "go!"))
				.createProperty(new PropertyInt("SamplePropertyIn", 12));

		Feature exp = new Feature("exp");
		exp.setFlippingStrategy(new ExpressionFlipStrategy("exp", "f1 & f2 | !f1 | f2"));
		ff4j.createFeature(exp);
		*/
		logger.info("FF4j: {}", ff4j);

		return ff4j;
	}

	@Bean
	public ApiConfig getApiConfig() {
		ApiConfig apiConfig = new ApiConfig();

		// Enable Authentication on API KEY
		apiConfig.setAuthenticate(authentication);
		apiConfig.createApiKey("apikey1a", true, true, Util.set("ADMIN", "USER"));
		apiConfig.createApiKey("apikey2a", true, true, Util.set("ADMIN", "USER"));
		apiConfig.createUser("user1", "password", true, true, Util.set("USER"));
		apiConfig.createUser("user2", "password", true, true, Util.set("ADMIN", "USER"));

		// Check Authorization as well
		apiConfig.setAutorize(authorization);
		apiConfig.setWebContext("/api");
		apiConfig.setPort(serverPort);
		apiConfig.setFF4j(getFF4j());
		apiConfig.setDocumentation(true);
		return apiConfig;
	}
}
