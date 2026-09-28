package kz.iitu.springlab.web;

import kz.iitu.springlab.config.AppProperties;
import kz.iitu.springlab.config.EnvironmentBanner;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/lab3")
public class Lab3Controller {

    private final AppProperties properties;
    private final EnvironmentBanner banner;
    private final Environment environment;

    public Lab3Controller(AppProperties properties, EnvironmentBanner banner, Environment environment) {
        this.properties = properties;
        this.banner = banner;
        this.environment = environment;
    }

    @GetMapping("/config")
    public Map<String, Object> config() {
        AppProperties.Mail mail = properties.mail();
        AppProperties.Security security = properties.security();

        return Map.ofEntries(
                Map.entry("owner", properties.owner()),
                Map.entry("group", properties.group()),
                Map.entry("mailFrom", mail.from()),
                Map.entry("mailRetryCount", mail.retryCount()),
                Map.entry("mailTimeout", mail.timeout().toString()),
                Map.entry("mailEnabled", mail.enabled()),
                Map.entry("securityTokenTtl", security.tokenTtl().toString()),
                Map.entry("securityMinPasswordLength", security.minPasswordLength()),
                Map.entry("serverPort", environment.getProperty("server.port")),
                Map.entry("activeProfiles", Arrays.asList(environment.getActiveProfiles())),
                Map.entry("banner", banner.describe())
        );
    }
}
