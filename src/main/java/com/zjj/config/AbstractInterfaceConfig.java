package com.zjj.config;

import com.zjj.common.JRpcURL;

import java.util.ArrayList;
import java.util.List;

public class AbstractInterfaceConfig extends AbstractConfig {
    private static final long serialVersionUID = -7564123152873135129L;

    protected List<JRpcURL> registryUrls = new ArrayList<>();

}
