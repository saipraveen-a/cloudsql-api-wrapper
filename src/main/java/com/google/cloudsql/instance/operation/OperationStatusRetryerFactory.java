package com.google.cloudsql.instance.operation;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import java.util.concurrent.TimeUnit;

public class OperationStatusRetryerFactory<T> {

  private static final int FIXED_WAIT_TIME_IN_SECS = 5;

  public Retryer<T> getStatusCheckRetryer(long stopDelayInMinutes) {
    return RetryerBuilder
        .<T>newBuilder()
        .retryIfException()
        .retryIfResult((Predicate<T>) Predicates.not(Predicates.equalTo(OperationStatus.DONE)))
        .withWaitStrategy(
            WaitStrategies.fixedWait(FIXED_WAIT_TIME_IN_SECS, TimeUnit.SECONDS))
        .withStopStrategy(StopStrategies.stopAfterDelay(stopDelayInMinutes, TimeUnit.MINUTES))
        .build();
  }
}
