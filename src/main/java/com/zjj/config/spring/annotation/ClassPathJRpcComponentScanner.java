package com.zjj.config.spring.annotation;

import com.zjj.common.JRpcURLParamType;
import com.zjj.common.utils.ReflectUtils;
import com.zjj.config.spring.ServiceBean;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
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
import java.util.StringJoiner;

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
    @NonNull
    public Set<BeanDefinitionHolder> doScan(@NonNull String... basePackages) {
        Assert.notEmpty(basePackages, "At least one base package must be specified");
        Set<BeanDefinitionHolder> beanDefinitionHolders = new LinkedHashSet<>();
        Arrays.stream(basePackages).forEach(basePackage -> {
            Set<BeanDefinition> beanDefinitions = findCandidateComponents(basePackage);
            beanDefinitions.stream()
                    .filter(b -> checkCandidate(BEAN_NAME_GENERATOR.generateBeanName(b, registry), b))
                    .forEach(beanDefinition -> {
                                String beanName = beanDefinition.getBeanClassName();
                                Assert.notNull(beanName, "beanName cannot be null.");
                                Class<?> beanClass = ClassUtils.resolveClassName(beanName, ReflectUtils.getClassLoader());
                                buildBeanDefinitionForServiceBean(beanDefinitionHolders, beanClass);
                            }
                    );
        });
        return beanDefinitionHolders;
    }

    /**
     * build beanDefinition for ref use type ServiceBean
     *
     * @param beanDefinitionHolders beanDefinitionHolders
     * @param beanClass             ref class type
     * @see com.zjj.config.spring.beans.JRpcInstantiationAwareBeanPostProcessor#postProcessProperties(PropertyValues, Object, String)
     */
    private void buildBeanDefinitionForServiceBean(Set<BeanDefinitionHolder> beanDefinitionHolders, Class<?> beanClass) {
        JRpcService service = beanClass.getDeclaredAnnotation(JRpcService.class);
        AnnotationAttributes annotationAttributes = AnnotationUtils.getAnnotationAttributes(null, service);
        Class<?> interfaceClass = obtainInterfaceClass(beanClass, annotationAttributes);
        // change bean name
        String beanName = rebuildBeanName(annotationAttributes, interfaceClass);
        // redefine beanDefinition
        Resource resource = getResourceLoader().getResource(SERVICE_BEAN_RESOURCE_PATH);
        try {
            MetadataReader metadataReader = getMetadataReaderFactory().getMetadataReader(resource);
            ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
            sbd.setBeanClass(ServiceBean.class);
            sbd.setResource(resource);
            sbd.setSource(resource);
            BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(sbd, beanName);
            beanDefinitionHolders.add(definitionHolder);
            registry.registerBeanDefinition(beanName, sbd);
        } catch (IOException e) {
            throw new BeanDefinitionStoreException(
                    "Failed to read candidate component class: " + resource, e);
        }
    }

    private String rebuildBeanName(AnnotationAttributes annotationAttributes, Class<?> interfaceClass) {
        StringJoiner stringJoiner = new StringJoiner(JRpcURLParamType.colon.getValue());
        return stringJoiner.add(ServiceBean.class.getSimpleName())
                .add(interfaceClass.getName())
                .add(annotationAttributes.getString(JRpcURLParamType.version.getName()))
                .add(annotationAttributes.getString(JRpcURLParamType.group.getName()))
                .toString();
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
