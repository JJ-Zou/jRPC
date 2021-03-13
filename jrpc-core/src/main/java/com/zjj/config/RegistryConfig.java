package com.zjj.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistryConfig extends AbstractConfig{
    private static final long serialVersionUID = 6807366295053407447L;

    private String name;
    private String registerProtocol;
    private String address;
    private Integer port;
}
