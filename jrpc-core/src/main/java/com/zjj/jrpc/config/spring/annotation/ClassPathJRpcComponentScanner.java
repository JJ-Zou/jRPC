package com.zjj.jrpc.config.spring.annotation;

import com.zjj.jrpc.common.JRpcURLParamType;
import com.zjj.jrpc.common.utils.BeanNameUtils;
import com.zjj.jrpc.common.utils.ReflectUtils;
import com.zjj.jrpc.config.spring.ServiceBean;
import com.zjj.jrpc.config.spring.beans.JRpcInstantiationAwareBeanPostProcessor;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.ManagedList;
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

import java.beans.Introspector;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class ClassPathJRpcComponentScanner extends ClassPathBeanDefinitionScanner {
    private static final String SERVICE_BEAN_RESOURCE_PATH =
            ResourceLoader.CLASSPATH_URL_PREFIX + ClassUtils.convertClassNameToResourcePath(ServiceBean.class.getName()) + ClassUtils.CLASS_FILE_SUFFIX;
    private static final BeanNameGenerator BEAN_NAME_GENERATOR = AnnotationBeanNameGenerator.INSTANCE;
    private final BeanDefinitionRegistry registry;

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
                            && ((AnnotatedBeanDefinition) b).getMetadata().hasAnnotation(JRpcService.class.getName()))
                    .forEach(beanDefinition -> {
                                String beanName = beanDefinition.getBeanClassName();
                                Assert.notNull(beanName, "beanName cannot be null.");
                                Class<?> beanClass = ClassUtils.resolveClassName(beanName, ReflectUtils.getClassLoader());
                                // fix: change bean name which ServiceBean inject
                                beanName = Introspector.decapitalize(ClassUtils.getShortName(beanClass));
                                // not only registry ServiceBean's BeanDefinition
                                if (checkCandidate(BEAN_NAME_GENERATOR.generateBeanName(beanDefinition, registry), beanDefinition)) {
                                    // fix: beanDefinition already exist, but need create ServiceBean BeanDefinition
                                    registry.registerBeanDefinition(beanName, beanDefinition);
                                }
                                BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(beanDefinition, beanName);
                                beanDefinitionHolders.add(definitionHolder);
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
            String exportProtocol = annotationAttributes.getString(JRpcURLParamType.EXPORT_PROTOCOL.getName());
            MutablePropertyValues propertyValues = sbd.getPropertyValues();
            propertyValues
                    .add("refBeanName", annotationBeanName)
                    // ServiceConfig
                    .add("interfaceClass", interfaceClass)
                    .add("refInterfaceName", interfaceClass.getName())
                    .add("ref", new RuntimeBeanReference(annotationBeanName))
                    .add("exportHost", annotationAttributes.getString(JRpcURLParamType.EXPORT_HOST.getName()))
                    .add("export", annotationAttributes.getBoolean(JRpcURLParamType.EXPORT.getName()))
                    // AbstractInterfaceConfig
                    .add("application", annotationAttributes.getString(JRpcURLParamType.APPLICATION.getName()))
                    .add("module", annotationAttributes.getString(JRpcURLParamType.MODULE.getName()))
                    .add("version", annotationAttributes.getString(JRpcURLParamType.VERSION.getName()))
                    .add("group", annotationAttributes.getString(JRpcURLParamType.GROUP.getName()))
            ;
            String[] protocolBeanNames = BeanNameUtils.getExportProtocolBeanName(exportProtocol);
            Assert.notEmpty(protocolBeanNames, "must config protocol!");
            ManagedList<RuntimeBeanReference> protocolConfigs = new ManagedList<>();
            Arrays.stream(protocolBeanNames)
                    .forEach(protocolBeanName -> protocolConfigs.add(new RuntimeBeanReference(protocolBeanName)));
            propertyValues.add("protocolConfigs", protocolConfigs);
            String exportRegistry = annotationAttributes.getString(JRpcURLParamType.EXPORT_REGISTRY.getName());
            String[] registryBeanNames = BeanNameUtils.getExportRegistryBeanName(exportRegistry);
            Assert.notEmpty(registryBeanNames, "must config registry!");
            ManagedList<RuntimeBeanReference> registryConfigs = new ManagedList<>();
            Arrays.stream(registryBeanNames)
                    .forEach(registryBeanName -> registryConfigs.add(new RuntimeBeanReference(registryBeanName)));
            propertyValues.add("registryConfigs", registryConfigs);
            registry.registerBeanDefinition(beanName, sbd);
        } catch (IOException e) {
            throw new BeanDefinitionStoreException(
                    "Failed to read candidate component class: " + resource, e);
        }
    }


    private Class<?> obtainInterfaceClass(Class<?> beanClass, AnnotationAttributes annotationAttributes) {
        Class<?> interfaceClass = annotationAttributes.getClass("interfaceClass");
        // default interfaceClass
        if (JRpcURLParamType.DEFAULT_CLASS.getType().equals(interfaceClass)) {
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
