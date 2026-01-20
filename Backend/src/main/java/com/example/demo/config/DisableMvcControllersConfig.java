package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.stereotype.Controller;

/**
 * DisableMvcControllersConfig
 *
 * When the 'api' profile is active this registry post-processor will remove beans
 * whose bean class is annotated with @Controller so the Thymeleaf/HTML controllers
 * are not registered. This allows running the app in API-only mode without
 * modifying each controller file.
 */
@Configuration
@Profile("api")
public class DisableMvcControllersConfig implements BeanDefinitionRegistryPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(DisableMvcControllersConfig.class);

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        String[] names = registry.getBeanDefinitionNames();
        for (String name : names) {
            try {
                String beanClassName = registry.getBeanDefinition(name).getBeanClassName();
                if (beanClassName == null) {
                    // sometimes factory methods / proxies - attempt to inspect metadata
                    var metadata = registry.getBeanDefinition(name).getSource();
                    if (metadata instanceof StandardMethodMetadata) {
                        beanClassName = ((StandardMethodMetadata) metadata).getReturnTypeName();
                    }
                }
                if (beanClassName == null) continue;
                Class<?> cls = Class.forName(beanClassName);
                if (cls.isAnnotationPresent(Controller.class)) {
                    registry.removeBeanDefinition(name);
                    log.info("Removed MVC controller bean (api profile): {}", beanClassName);
                }
            } catch (ClassNotFoundException e) {
                // ignore
            } catch (Exception e) {
                log.debug("Error inspecting bean {}: {}", name, e.getMessage());
            }
        }
    }

    @Override
    public void postProcessBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // no-op
    }
}


