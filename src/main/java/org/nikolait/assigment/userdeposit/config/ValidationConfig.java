package org.nikolait.assigment.userdeposit.config;

import lombok.RequiredArgsConstructor;
import org.nikolait.assigment.userdeposit.validation.FixedLocaleMessageInterpolator;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
@RequiredArgsConstructor
public class ValidationConfig {

    private final WebProperties webProperties;

    @Bean
    public LocalValidatorFactoryBean validatorFactory() {
        LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
        factoryBean.afterPropertiesSet();
        factoryBean.setMessageInterpolator(new FixedLocaleMessageInterpolator(
                factoryBean.getMessageInterpolator(),
                webProperties.getLocale()));
        return factoryBean;
    }
}
