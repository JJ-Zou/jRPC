package com.zjj.config;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BaseReferenceConfig extends AbstractReferenceConfig {
    private static final long serialVersionUID = 4059589984503217585L;
    private Boolean isDefault;
}
