package photogift.server.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import photogift.server.controller.Controllers;
import photogift.server.error.ErrorInfo;

@Configuration
@EnableWebMvc
@ComponentScan(basePackageClasses = {Controllers.class, ErrorInfo.class})
public class MvcConfig extends WebMvcConfigurerAdapter {
}
