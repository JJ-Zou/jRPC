package com.zjj.config;

import com.zjj.config.annotation.Ignore;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProtocolConfig extends AbstractConfig {

    private static final long serialVersionUID = -8136906908356538696L;
    // 服务协议
    private String protocolName;
    private String clutter;
    private String loadBalance;
    private String haStrategy;
    private String proxy;
    @Ignore
    private boolean isDefault;

}
