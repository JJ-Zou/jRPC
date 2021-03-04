package com.zjj.config;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BaseServiceConfig extends AbstractServiceConfig {
    private static final long serialVersionUID = -5540157227970685636L;
    private Boolean isDefault;
}
