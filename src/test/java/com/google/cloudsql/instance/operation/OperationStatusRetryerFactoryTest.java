package com.google.cloudsql.instance.operation;

import static org.junit.Assert.assertNotNull;

import com.github.rholder.retry.Retryer;

import org.junit.Test;

public class OperationStatusRetryerFactoryTest {

  private static final long STOP_DELAY_IN_MINUTES = 5L;

  @Test
  public void getStatusCheckRetryer_whenGetStatusCheckRetryer_thenReturnsExpectedRetryer() {
    OperationStatusRetryerFactory<String> retryerFactory = new OperationStatusRetryerFactory<>();
    Retryer<String> retryer = retryerFactory.getStatusCheckRetryer(STOP_DELAY_IN_MINUTES);
    assertNotNull(retryer);
  }
}
