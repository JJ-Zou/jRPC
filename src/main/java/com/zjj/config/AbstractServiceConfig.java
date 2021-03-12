package com.zjj.config;

import com.zjj.common.JRpcURLParamType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class AbstractServiceConfig extends AbstractInterfaceConfig {
    private static final long serialVersionUID = 3050375292373047800L;

    protected String exportProtocol;
    protected String exportHost;
    protected boolean isDefault;

    public abstract void export();

    public abstract void unExport();

    public abstract boolean isExported();

    public abstract boolean isUnexported();


    /**
     * 将 exportProtocol 解析为map
     *
     * @return map -> (key = protocol_id, value = export_port)
     */
    protected Map<String, Integer> checkAndGetProtocol() {
        if (StringUtils.isEmpty(exportProtocol)) {
            throw new IllegalStateException("Service exportProtocol must be set.");
        }
        return Arrays.stream(JRpcURLParamType.commaSplitPattern.getPattern().split(exportProtocol))
                .map(protocol -> JRpcURLParamType.colonSplitPattern.getPattern().split(protocol))
                .collect(Collectors.toMap(protocolArr -> protocolArr[0], protocolArr -> Integer.parseInt(protocolArr[1])));
    }
}
