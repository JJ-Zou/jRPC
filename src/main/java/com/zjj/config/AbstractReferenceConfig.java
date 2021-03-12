package com.zjj.config;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class AbstractReferenceConfig extends AbstractInterfaceConfig{
    private static final long serialVersionUID = 7573895435781624978L;
    private Boolean isDefault;

}
