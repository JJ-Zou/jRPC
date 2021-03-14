package com.zjj.jrpc.config.spring.beans.definition;

import com.zjj.jrpc.common.utils.JRpcComponentUtils;
import com.zjj.jrpc.config.spring.annotation.ClassPathJRpcComponentScanner;
import com.zjj.jrpc.config.spring.annotation.JRpcComponent;
import com.zjj.jrpc.config.spring.annotation.JRpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;

import java.util.*;

@Slf4j
public class JRpcServiceBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {


    /**
     * @param registry BeanDefinitionRegistry
     * @throws BeansException throw exception
     * @see ClassPathJRpcComponentScanner#doScan(String...)
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        List<BeanDefinitionHolder> componentScanCandidates = new ArrayList<>();
        String[] candidateNames = registry.getBeanDefinitionNames();
        Arrays.stream(candidateNames).forEach(beanName -> {
            BeanDefinition beanDef = registry.getBeanDefinition(beanName);
            if (JRpcComponentUtils.checkJRpcComponentScanCandidate(beanDef)) {
                componentScanCandidates.add(new BeanDefinitionHolder(beanDef, beanName));
            }
        });
        // Return immediately if no @JRpcComponentScan classes were found
        if (componentScanCandidates.isEmpty()) {
            return;
        }
        Set<String> packagesToScan = new LinkedHashSet<>();
        componentScanCandidates.forEach(beanDefinitionHolder -> {
            AnnotatedBeanDefinition beanDef = (AnnotatedBeanDefinition) beanDefinitionHolder.getBeanDefinition();
            AnnotationMetadata metadata = beanDef.getMetadata();
            AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(JRpcComponent.class.getName()));
            if (annotationAttributes != null) {
                String[] values = annotationAttributes.getStringArray("value");
                List<String> list = Arrays.asList(values.clone());
                packagesToScan.addAll(list);
                if (packagesToScan.isEmpty()) {
                    packagesToScan.add(ClassUtils.getPackageName(metadata.getClassName()));
                }
            }
        });
        if (packagesToScan.isEmpty()) {
            log.warn("JRpcComponent scan no package, it indicates there is annotation named @JRpcComponent!");
        }
        // register ServiceBean
        ClassPathBeanDefinitionScanner scanner = new ClassPathJRpcComponentScanner(registry);
        scanner.addIncludeFilter(new AnnotationTypeFilter(JRpcService.class));
        int scan = scanner.scan(packagesToScan.toArray(new String[0]));
        log.info("Find rpc service in package {}, count: {}", packagesToScan, scan);
    }

    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
        log.info("BeanFactory: {}", beanFactory);
    }

}
