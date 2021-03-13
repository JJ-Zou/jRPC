package com.zjj.config.spring.annotation;

import com.zjj.common.JRpcURLParamType;
import com.zjj.common.utils.BeanNameUtils;
import com.zjj.common.utils.ReflectUtils;
import com.zjj.config.spring.ServiceBean;
import com.zjj.config.spring.beans.JRpcInstantiationAwareBeanPostProcessor;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class ClassPathJRpcComponentScanner extends ClassPathBeanDefinitionScanner {
    private static final String SERVICE_BEAN_RESOURCE_PATH =
            ResourceLoader.CLASSPATH_URL_PREFIX + ClassUtils.convertClassNameToResourcePath(ServiceBean.class.getName()) + ClassUtils.CLASS_FILE_SUFFIX;

    private final BeanDefinitionRegistry registry;

    private static final BeanNameGenerator BEAN_NAME_GENERATOR = AnnotationBeanNameGenerator.INSTANCE;

    public ClassPathJRpcComponentScanner(BeanDefinitionRegistry registry) {
        super(registry);
        this.registry = registry;
    }

    @Override
    public int scan(@NonNull String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitionHolders = doScan(basePackages);
        return beanDefinitionHolders.size();
    }

    @Override
    @NonNull
    public Set<BeanDefinitionHolder> doScan(@NonNull String... basePackages) {
        Assert.notEmpty(basePackages, "At least one base package must be specified");
        Set<BeanDefinitionHolder> beanDefinitionHolders = new LinkedHashSet<>();
        Arrays.stream(basePackages).forEach(basePackage -> {
            Set<BeanDefinition> beanDefinitions = findCandidateComponents(basePackage);
            beanDefinitions.stream()
                    .filter(b -> (b instanceof AnnotatedBeanDefinition)
                            && ((AnnotatedBeanDefinition) b).getMetadata().hasAnnotation(JRpcService.class.getName())
                            && checkCandidate(BEAN_NAME_GENERATOR.generateBeanName(b, registry), b))
                    .forEach(beanDefinition -> {
                                String beanName = beanDefinition.getBeanClassName();
                                Assert.notNull(beanName, "beanName cannot be null.");
                                Class<?> beanClass = ClassUtils.resolveClassName(beanName, ReflectUtils.getClassLoader());
                                // not only registry ServiceBean's BeanDefinition
                                BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(beanDefinition, beanName);
                                beanDefinitionHolders.add(definitionHolder);
                                registry.registerBeanDefinition(beanName, beanDefinition);
                                // but don't scan
                                buildBeanDefinitionForServiceBean(beanClass, beanName);
                            }
                    );
        });
        return beanDefinitionHolders;
    }

    /**
     * build beanDefinition for ref use type ServiceBean
     *
     * @param beanClass          ref class type
     * @param annotationBeanName @JRpcService bean name
     * @see JRpcInstantiationAwareBeanPostProcessor#postProcessProperties(PropertyValues, Object, String)
     */
    private void buildBeanDefinitionForServiceBean(Class<?> beanClass, String annotationBeanName) {
        JRpcService service = beanClass.getDeclaredAnnotation(JRpcService.class);
        AnnotationAttributes annotationAttributes = AnnotationUtils.getAnnotationAttributes(service, false, false);
        Class<?> interfaceClass = obtainInterfaceClass(beanClass, annotationAttributes);
        // change bean name
        String beanName = BeanNameUtils.buildServiceBeanName(annotationAttributes, interfaceClass);
        // redefine beanDefinition
        Resource resource = getResourceLoader().getResource(SERVICE_BEAN_RESOURCE_PATH);
        try {
            MetadataReader metadataReader = getMetadataReaderFactory().getMetadataReader(resource);
            ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
            sbd.setBeanClass(ServiceBean.class);
            sbd.setResource(resource);
            sbd.setSource(resource);
            String exportProtocol = annotationAttributes.getString(JRpcURLParamType.exportProtocol.getName());
            MutablePropertyValues propertyValues = sbd.getPropertyValues();
            propertyValues
                    .add("beanName", beanName)
                    // ServiceConfig
                    .add("interfaceClass", interfaceClass)
                    .add("interfaceName", interfaceClass.getName())
                    .add("ref", new RuntimeBeanReference(annotationBeanName))
                    .add("exportProtocol", exportProtocol)
                    .add("exportHost", annotationAttributes.getString(JRpcURLParamType.exportHost.getName()))
                    .add("export", annotationAttributes.getBoolean(JRpcURLParamType.export.getName()))
                    // AbstractInterfaceConfig
                    .add("application", annotationAttributes.getString(JRpcURLParamType.application.getName()))
                    .add("module", annotationAttributes.getString(JRpcURLParamType.module.getName()))
                    .add("version", annotationAttributes.getString(JRpcURLParamType.version.getName()))
                    .add("group", annotationAttributes.getString(JRpcURLParamType.group.getName()))
            ;
            String[] protocolBeanNames = BeanNameUtils.getExportProtocolBeanName(exportProtocol);
            Arrays.stream(protocolBeanNames)
                    .forEach(protocolBeanName -> propertyValues.add("protocolConfigs", new RuntimeBeanReference(protocolBeanName)));
            String exportRegistry = annotationAttributes.getString(JRpcURLParamType.exportRegistry.getName());
            String[] registryBeanNames = BeanNameUtils.getExportRegistryBeanName(exportRegistry);
            Arrays.stream(registryBeanNames)
                    .forEach(registryBeanName -> propertyValues.add("registryConfigs", new RuntimeBeanReference(registryBeanName)));
            registry.registerBeanDefinition(beanName, sbd);
        } catch (IOException e) {
            throw new BeanDefinitionStoreException(
                    "Failed to read candidate component class: " + resource, e);
        }
    }


    private Class<?> obtainInterfaceClass(Class<?> beanClass, AnnotationAttributes annotationAttributes) {
        Class<?> interfaceClass = annotationAttributes.getClass("interfaceClass");
        // default interfaceClass
        if (JRpcURLParamType.defaultClass.getType().equals(interfaceClass)) {
            interfaceClass = null;
            String interfaceName = annotationAttributes.getString("interfaceName");
            if (!StringUtils.isEmpty(interfaceName) && ClassUtils.isPresent(interfaceName, ReflectUtils.getClassLoader())) {
                interfaceClass = ClassUtils.resolveClassName(interfaceName, ReflectUtils.getClassLoader());
            }
        }
        if (interfaceClass == null) {
            interfaceClass = beanClass.getInterfaces()[0];
        }
        return interfaceClass;
    }
}
