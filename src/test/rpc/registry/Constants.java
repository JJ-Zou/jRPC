package com.zjj.rpc.registry;

public interface Constants {
    String REGISTRY_LOCAL_FILE_CACHE_ENABLED = "file.cache";


    String RETRY_REGISTER_NAME = "retry register";
    String RETRY_UNREGISTER_NAME = "retry unregister";
    String RETRY_SUBSCRIBE_NAME = "retry subscribe";
    String RETRY_UNSUBSCRIBE_NAME = "retry unsubscribe";
    String RETRY_NOTIFY_NAME = "retry notify";
    String REGISTRY_RETRY_PERIOD_KEY = "retry.period";
    int DEFAULT_REGISTRY_RETRY_PERIOD = 5 * 1000;
    String REGISTRY_RETRY_TIMES_KEY = "retry.times";
    int DEFAULT_REGISTRY_RETRY_TIMES = 3;
    String DUBBO_REGISTRY_RETRY_TIMER_KEY = "DubboRegistryRetryTimer";
    String CHECK_KEY = "check";
    String CONSUMER_PROTOCOL = "consumer";
}
