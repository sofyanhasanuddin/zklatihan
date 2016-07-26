package org.sofyan.latihan;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ErrorPage;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.zkoss.zk.au.http.DHtmlUpdateServlet;
import org.zkoss.zk.ui.http.DHtmlLayoutServlet;

@SpringBootApplication
@Import({SecurityConfig.class,WebConfig.class})
@EnableAutoConfiguration
public class ZklatihanApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(ZklatihanApplication.class, args);
	}
	
	@Value("classpath:dbscript/insert.sql")
	private Resource dataScript;
	
	@Autowired
	private DataSource dataSource;
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(ZklatihanApplication.class);
	}
	
	@Bean
	public ServletRegistrationBean dHtmlLayoutServlet() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("update-uri", "/zkau");
		DHtmlLayoutServlet dHtmlLayoutServlet = new DHtmlLayoutServlet();
		ServletRegistrationBean reg = new ServletRegistrationBean(dHtmlLayoutServlet, "*.zul");
		reg.setLoadOnStartup(1);
		reg.setInitParameters(params);
		return reg;
	}

	@Bean
	public ServletRegistrationBean dHtmlUpdateServlet() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("update-uri", "/zkau/*");
		ServletRegistrationBean reg = new ServletRegistrationBean(new DHtmlUpdateServlet(), "/zkau/*");
		reg.setLoadOnStartup(2);
		reg.setInitParameters(params);
		return reg;
	}
	
	@Bean
	public EmbeddedServletContainerCustomizer containerCustomizer() {
	 
	    return new EmbeddedServletContainerCustomizer() {
	        @Override
	        public void customize(ConfigurableEmbeddedServletContainer container) {

	        	ErrorPage error403Page = new ErrorPage(HttpStatus.FORBIDDEN, "/handleAccessDenied.zul");
	 
	            container.addErrorPages( error403Page );
	        }
	    };
	}
	
	@EventListener({ContextRefreshedEvent.class})
    void contextRefreshedEvent() {
		
		ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.addScript( dataScript );
		populator.setContinueOnError(true);
		
		DatabasePopulatorUtils.execute(populator, dataSource);
		
    }
	
//	@Bean
//	public InstrumentationLoadTimeWeaver loadTimeWeaver()  throws Throwable {
//	    InstrumentationLoadTimeWeaver loadTimeWeaver = new InstrumentationLoadTimeWeaver();
//	    return loadTimeWeaver;
//	}
	
}
