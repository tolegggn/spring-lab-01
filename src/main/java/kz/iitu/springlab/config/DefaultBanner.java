package kz.iitu.springlab.config;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!dev & !prod")
class DefaultBanner implements EnvironmentBanner {
    @Override
    public String describe() {
        return "no profile is active";
    }
}
