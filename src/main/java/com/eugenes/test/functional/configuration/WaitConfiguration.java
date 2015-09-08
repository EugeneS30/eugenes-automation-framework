package com.eugenes.test.functional.configuration;

import java.util.concurrent.TimeUnit;

import lombok.Value;
import lombok.experimental.Builder;

@Builder
@Value
public final class WaitConfiguration {

    private TimeUnit timeoutUnit;
    private int timeoutTime;

    public long getTimeoutInMillis() {
        return timeoutUnit.toMillis(timeoutTime);
    }
}
