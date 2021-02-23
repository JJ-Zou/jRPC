package com.zjj.common.utils;

import com.zjj.common.URL;
import com.zjj.common.constants.CommonConstants;
import com.zjj.common.constants.RegistryConstants;

public class UrlUtils {

    public static boolean isMatch(URL consumerUrl, URL providerUrl) {
        String consumerInterface = consumerUrl.getServiceInterface();
        String providerInterface = providerUrl.getServiceInterface();
        if (!(CommonConstants.ANY_VALUE.equals(consumerInterface) || CommonConstants.ANY_VALUE.equals(providerInterface) || StringUtils.isEquals(consumerInterface, providerInterface))) {
            return false;
        }
        if (!isMatchCategory(providerUrl.getParameter(RegistryConstants.CATEGORY_KEY, RegistryConstants.DEFAULT_CATEGORY), consumerUrl.getParameter(RegistryConstants.CATEGORY_KEY, RegistryConstants.DEFAULT_CATEGORY))) {
            return false;
        }
        if (!providerUrl.getParameter(CommonConstants.ENABLED_KEY, true) && !CommonConstants.ANY_VALUE.equals(consumerUrl.getParameter(CommonConstants.ENABLED_KEY))) {
            return false;
        }
        String consumerGroup = consumerUrl.getParameter(CommonConstants.GROUP_KEY);
        String consumerVersion = consumerUrl.getParameter(CommonConstants.VERSION_KEY);
        String consumerClassifier = consumerUrl.getParameter(CommonConstants.CLASSIFIER_KEY, CommonConstants.ANY_VALUE);
        String providerGroup = providerUrl.getParameter(CommonConstants.GROUP_KEY);
        String providerVersion = providerUrl.getParameter(CommonConstants.VERSION_KEY);
        String providerClassifier = providerUrl.getParameter(CommonConstants.CLASSIFIER_KEY, CommonConstants.ANY_VALUE);
        return (CommonConstants.ANY_VALUE.equals(consumerGroup)) || StringUtils.isEquals(consumerGroup, providerGroup) || StringUtils.isContains(consumerGroup, providerGroup)
                && (CommonConstants.ANY_VALUE.equals(consumerVersion) || StringUtils.isEquals(consumerVersion, providerVersion))
                && (consumerClassifier == null || CommonConstants.ANY_VALUE.equals(consumerClassifier) || StringUtils.isEquals(consumerClassifier, providerClassifier));

    }

    public static boolean isMatchCategory(String category, String categories) {
        if (StringUtils.isEmpty(categories)) {
            return RegistryConstants.DEFAULT_CATEGORY.equals(category);
        }
        if (categories.contains(CommonConstants.ANY_VALUE)) {
            return true;
        }
        if (categories.contains(CommonConstants.REMOVE_VALUE_PREFIX)) {
            return !categories.contains(CommonConstants.REMOVE_VALUE_PREFIX + category);
        }
        return categories.contains(category);
    }
}
