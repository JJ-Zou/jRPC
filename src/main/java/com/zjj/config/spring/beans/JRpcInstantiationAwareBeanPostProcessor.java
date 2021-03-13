package com.zjj.config.spring.beans;

import com.zjj.common.utils.BeanNameUtils;
import com.zjj.config.AbstractConfig;
import com.zjj.config.ProtocolConfig;
import com.zjj.config.RegistryConfig;
import com.zjj.config.spring.ReferenceBean;
import com.zjj.config.spring.annotation.JRpcReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class JRpcInstantiationAwareBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter
        implements MergedBeanDefinitionPostProcessor, BeanFactoryAware {

    private final Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<>();
    private final Map<String, Object> injectObjectsCache = new ConcurrentHashMap<>();
    private final Map<String, ReferenceBean<?>> referenceBeansCache = new ConcurrentHashMap<>();

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void postProcessMergedBeanDefinition(@NonNull RootBeanDefinition beanDefinition, @NonNull Class<?> beanType, @NonNull String beanName) {
        InjectionMetadata referenceMetadata = findReferenceMetadata(beanName, beanType, null);
        referenceMetadata.checkConfigMembers(beanDefinition);
    }

    @Override
    public PropertyValues postProcessProperties(@NonNull PropertyValues pvs, @NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (bean instanceof AbstractConfig) {
            // AbstractConfig
            ((MutablePropertyValues) pvs).add("id", beanName);
        }
        InjectionMetadata referenceMetadata = findReferenceMetadata(beanName, bean.getClass(), pvs);
        try {
            referenceMetadata.inject(bean, beanName, pvs);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return null;
    }

    private InjectionMetadata findReferenceMetadata(String beanName, Class<?> clazz, @Nullable PropertyValues pvs) {
        // Fall back to class name as cache key, for backwards compatibility with custom callers.
        String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
        // Quick check on the concurrent map first, with minimal locking.
        InjectionMetadata metadata = injectionMetadataCache.get(cacheKey);
        if (InjectionMetadata.needsRefresh(metadata, clazz)) {
            synchronized (this.injectionMetadataCache) {
                metadata = this.injectionMetadataCache.get(cacheKey);
                if (InjectionMetadata.needsRefresh(metadata, clazz)) {
                    if (metadata != null) {
                        metadata.clear(pvs);
                    }
                    metadata = findFieldAnnotation(clazz);
                    this.injectionMetadataCache.put(cacheKey, metadata);
                }
            }
        }
        return metadata;
    }

    private InjectionMetadata findFieldAnnotation(final Class<?> clazz) {
        List<InjectionMetadata.InjectedElement> currElements = new ArrayList<>();
        ReflectionUtils.doWithLocalFields(clazz, field -> {
            JRpcReference reference = field.getAnnotation(JRpcReference.class);
            if (reference != null) {
                if (Modifier.isFinal(field.getModifiers())) {
                    return;
                }
                AnnotationAttributes attributes = AnnotationAttributes.fromMap(AnnotationUtils.getAnnotationAttributes(reference));
                currElements.add(new FiledElement(field, attributes));
            }
        });
        return InjectionMetadata.forElements(currElements, clazz);
    }

    protected Object getInjectObject(AnnotationAttributes attributes, Object bean, String beanName, Class<?> type, InjectionMetadata.InjectedElement metadata) {
        String injectKey = getInjectKey(attributes, type, metadata);
        return injectObjectsCache.computeIfAbsent(injectKey, o -> doGetInjectObject(attributes, bean, beanName, type, metadata));
    }

    protected String getInjectKey(AnnotationAttributes attributes, Class<?> type, InjectionMetadata.InjectedElement metadata) {
        StringJoiner joiner = new StringJoiner("#");
        joiner.add(BeanNameUtils.buildServiceBeanName(attributes, type))
                .add("ref=" + metadata.getMember())
                .add("attributes=" + attributes);
        return joiner.toString();
    }

    protected Object doGetInjectObject(AnnotationAttributes attributes, Object bean, String beanName, Class<?> type, InjectionMetadata.InjectedElement metadata) {
        String serviceName = BeanNameUtils.buildServiceBeanName(attributes, type);
        String referName = BeanNameUtils.buildReferenceBeanName(attributes, type);
        return referenceBeansCache
                .computeIfAbsent(referName, r -> createRef(referName, attributes, type))
                .getRef();
    }

    private ReferenceBean createRef(String referenceBeanName, AnnotationAttributes attributes, Class<?> type) {
        ReferenceBean referenceBean = new ReferenceBean();
        referenceBean.setInterfaceClass(type);
        RegistryConfig registryConfig = beanFactory.getBean(RegistryConfig.class);
        ProtocolConfig protocolConfig = beanFactory.getBean(ProtocolConfig.class);
        return referenceBean;
    }


    private class FiledElement extends InjectionMetadata.InjectedElement {
        private final Field field;
        private final AnnotationAttributes attributes;

        protected FiledElement(Field field, AnnotationAttributes attributes) {
            super(field, null);
            this.field = field;
            this.attributes = attributes;
        }

        @Override
        protected void inject(@NonNull Object bean, String requestingBeanName, PropertyValues pvs) throws Throwable {
            Class<?> fieldType = field.getType();
            Object injectObject = getInjectObject(attributes, bean, requestingBeanName, fieldType, this);
            ReflectionUtils.makeAccessible(field);
            field.set(bean, injectObject);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            FiledElement that = (FiledElement) o;
            return Objects.equals(field, that.field);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), field);
        }
    }

}
