package com.zjj.rpc.config.annotation;

import com.zjj.rpc.config.spring.ServiceBean;
import com.zjj.rpc.config.spring.context.JRpcBootstrapApplicationListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.*;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
public class ServiceClassPostProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware,
        BeanClassLoaderAware, ResourceLoaderAware {

    private static final Class<? extends Annotation> SERVICE_ANNOTATION = JRpcService.class;

    protected final Set<String> packagesToScan;

    private Environment environment;

    private ClassLoader classLoader;

    private ResourceLoader resourceLoader;

    public ServiceClassPostProcessor(Set<String> packagesToScan) {
        this.packagesToScan = packagesToScan;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        registerJRpcBootstrapApplicationListener(registry, JRpcBootstrapApplicationListener.BEAN_NAME, JRpcBootstrapApplicationListener.class);
        Set<String> resolvedPackagesToScan = resolvePackagesToScan();
        if (resolvedPackagesToScan.isEmpty()) {
            log.warn("packagesToScan is empty , ServiceBean registry will be ignored!");
            return;
        }
        registerServiceBeans(resolvedPackagesToScan, registry);
    }

    private void registerJRpcBootstrapApplicationListener(BeanDefinitionRegistry registry, String beanName, Class<?> type) {
        if (!registry.containsBeanDefinition(beanName)) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(type);
            builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            AbstractBeanDefinition rootBeanDefinition = builder.getBeanDefinition();
            registry.registerBeanDefinition(beanName, rootBeanDefinition);
            log.info("The Infrastructure bean definition [{}] with name [{}] has been registered.", rootBeanDefinition, beanName);
        }
    }

    private void registerServiceBeans(Set<String> resolvedPackagesToScan, BeanDefinitionRegistry registry) {
        JRpcClassPathBeanDefinitionScanner scanner =
                new JRpcClassPathBeanDefinitionScanner(registry, environment, resourceLoader);
        BeanNameGenerator beanNameGenerator = resolveBeanNameGenerator(registry);
        scanner.setBeanNameGenerator(beanNameGenerator);
        scanner.addIncludeFilter(new AnnotationTypeFilter(SERVICE_ANNOTATION));
        for (String packageToScan : resolvedPackagesToScan) {
            scanner.scan(packageToScan);
            Set<BeanDefinitionHolder> serviceBeanDefinitionHolders =
                    findServiceBeanDefinitionHolders(scanner, packageToScan, registry, beanNameGenerator);
            if (CollectionUtils.isEmpty(serviceBeanDefinitionHolders)) {
                log.warn("No Spring Bean annotating @JRpcService was found under package [{}].", packagesToScan);
            } else {
                for (BeanDefinitionHolder serviceBeanDefinitionHolder : serviceBeanDefinitionHolders) {
                    registerServiceBean(serviceBeanDefinitionHolder, registry, scanner);
                }
            }
        }
    }

    private void registerServiceBean(BeanDefinitionHolder beanDefinitionHolder,
                                     BeanDefinitionRegistry registry, JRpcClassPathBeanDefinitionScanner scanner) {
        Class<?> beanClass = ClassUtils.resolveClassName(beanDefinitionHolder.getBeanDefinition().getBeanClassName(), classLoader);
        Annotation service = AnnotatedElementUtils.findMergedAnnotation(beanClass, SERVICE_ANNOTATION);
        AnnotationAttributes serviceAnnotationAttributes = AnnotationUtils.getAnnotationAttributes(service, false, false);
        Class<?> interfaceClass = serviceAnnotationAttributes.getClass("interfaceClass");
        String annotatedServiceBeanName = beanDefinitionHolder.getBeanName();
        AbstractBeanDefinition serviceBeanDefinition =
                buildServiceBeanDefinition(service, serviceAnnotationAttributes, interfaceClass, annotatedServiceBeanName);
        String beanName = generateServiceBeanName(serviceAnnotationAttributes, interfaceClass);
        if (scanner.checkCandidate(beanName, serviceBeanDefinition)) {
            registry.registerBeanDefinition(beanName, serviceBeanDefinition);
            log.info("The BeanDefinition [{}] of ServiceBean has been registered with name : [{}]", serviceBeanDefinition, beanName);
        } else {
            log.warn("The Duplicated BeanDefinition [{}]  of ServiceBean[ bean name : {} was be found , Did @RpcComponentScan scan to same package in many times?",
                    serviceBeanDefinition, beanName);
        }
    }

    private AbstractBeanDefinition buildServiceBeanDefinition(Annotation service, AnnotationAttributes serviceAnnotationAttributes, Class<?> interfaceClass, String annotatedServiceBeanName) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ServiceBean.class);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        return beanDefinition;
    }

    private String generateServiceBeanName(AnnotationAttributes serviceAnnotationAttributes, Class<?> interfaceClass) {
        return "ServiceBean:" +
                interfaceClass.getName() +
                ":" +
                serviceAnnotationAttributes.getString("group") +
                ":" +
                serviceAnnotationAttributes.getString("version");
    }

    private Set<BeanDefinitionHolder> findServiceBeanDefinitionHolders(ClassPathBeanDefinitionScanner scanner,
                                                                       String packageToScan,
                                                                       BeanDefinitionRegistry registry,
                                                                       BeanNameGenerator beanNameGenerator) {
        Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(packageToScan);
        Set<BeanDefinitionHolder> beanDefinitionHolders = new LinkedHashSet<>(beanDefinitions.size());
        for (BeanDefinition beanDefinition : beanDefinitions) {
            String beanName = beanNameGenerator.generateBeanName(beanDefinition, registry);
            BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(beanDefinition, beanName);
            beanDefinitionHolders.add(beanDefinitionHolder);
        }
        return beanDefinitionHolders;
    }

    private BeanNameGenerator resolveBeanNameGenerator(BeanDefinitionRegistry registry) {
        BeanNameGenerator beanNameGenerator = null;
        if (registry instanceof SingletonBeanRegistry) {
            SingletonBeanRegistry singletonBeanRegistry = (SingletonBeanRegistry) registry;
            beanNameGenerator = (BeanNameGenerator) singletonBeanRegistry.getSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR);
        }

        if (beanNameGenerator == null) {
            log.info("BeanNameGenerator bean can't be found in BeanFactory with name [{}].", AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR);
            log.info("BeanNameGenerator will be a instance of {} , it maybe a potential problem on bean name generation.", AnnotationBeanNameGenerator.class.getName());
            beanNameGenerator = new AnnotationBeanNameGenerator();
        }

        return beanNameGenerator;
    }

    private Set<String> resolvePackagesToScan() {
        Set<String> resolvedPackagesToScan = new LinkedHashSet<>(packagesToScan.size());
        for (String packageToScan : packagesToScan) {
            String resolvePlaceholders = environment.resolvePlaceholders(packageToScan.trim());
            resolvedPackagesToScan.add(resolvePlaceholders);
        }
        return resolvedPackagesToScan;
    }

    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
