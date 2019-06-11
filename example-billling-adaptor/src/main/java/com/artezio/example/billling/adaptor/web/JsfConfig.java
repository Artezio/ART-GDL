/*
 */
package com.artezio.example.billling.adaptor.web;

import javax.faces.webapp.FacesServlet;
import org.apache.catalina.Context;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
@Configuration
public class JsfConfig extends SpringBootServletInitializer implements WebMvcConfigurer {

    /**
     * JSF Servlet web context registration bean.
     *
     * @return Servlet registration bean.
     */
    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        FacesServlet servlet = new FacesServlet();
        ServletRegistrationBean reg = new ServletRegistrationBean(servlet, "*.xhtml");
        return reg;
    }

    /**
     * JSF filter registration bean.
     *
     * @return Filter registration bean.
     */
    @Bean
    public FilterRegistrationBean loginFilter() {
        FilterRegistrationBean reg = new FilterRegistrationBean();
        reg.setFilter(new SessionExpiredFilter());
        reg.addUrlPatterns("*.xhtml");
        return reg;
    }

    /**
     * Embedded Apache Tomcat configuration bean.
     *
     * @return Embedded server configuration bean.
     */
    @Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        return new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                ((StandardJarScanner) context.getJarScanner()).setScanManifest(false);
            }
        };
    }

    /**
     * Web context navigation rules applier.
     *
     * @param registry View controller registry access bean.
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.xhtml");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }
}
