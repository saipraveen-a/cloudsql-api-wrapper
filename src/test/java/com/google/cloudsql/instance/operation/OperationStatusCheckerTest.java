package com.google.cloudsql.instance.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloudsql.exception.CloudSqlException;

import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.api.services.sqladmin.SQLAdmin;
import com.google.api.services.sqladmin.model.Operation;
import com.google.common.base.Predicates;

import org.junit.Before;
import org.junit.Test;
import org.mockito.verification.VerificationMode;

import java.io.IOException;

public class OperationStatusCheckerTest {
  private static final String PROJECT_ID = "my-peoject";
  private static final long WAIT_TIME_IN_MILLIS = 10L;
  private static final int TOTAL_FAILURE_ATTEMPTS = 5;
  private static final VerificationMode IS_INVOKED_THREE_TIMES = times(3);
  private static final VerificationMode IS_INVOKED_FIVE_TIMES = times(5);

  private SQLAdmin sqlAdmin;
  private Operation operation;
  private SQLAdmin.Operations operations;
  private SQLAdmin.Operations.Get getRequest;
  private OperationStatusRetryerFactory<OperationStatus> statusCheckRetryer;

  @Before
  public void setUp() {
    sqlAdmin = mock(SQLAdmin.class);
    operation = givenOperation();
    operations = mock(SQLAdmin.Operations.class);
    getRequest = mock(SQLAdmin.Operations.Get.class);
    statusCheckRetryer = mock(OperationStatusRetryerFactory.class);
    when(sqlAdmin.operations()).thenReturn(operations);
    Retryer<OperationStatus> retryer = givenRetryer();
    when(statusCheckRetryer.getStatusCheckRetryer(anyLong())).thenReturn(retryer);
  }

  private Operation givenOperation() {
    Operation operation = new Operation();
    operation.setName("get-operation");
    operation.setStatus("PENDING");
    return operation;
  }

  private Retryer givenRetryer() {
    Retryer<OperationStatus> retryer = RetryerBuilder
        .<OperationStatus>newBuilder()
        .retryIfException()
        .retryIfResult(Predicates.not(Predicates.equalTo(OperationStatus.DONE)))
        .withWaitStrategy(WaitStrategies.exponentialWait())
        .withStopStrategy(StopStrategies.stopAfterAttempt(TOTAL_FAILURE_ATTEMPTS))
        .build();
    return retryer;
  }

  @Test
  public void checkStatus_whenSqlAdminApiIsSuccessful_thenReturnsStatus() throws Exception {
    when(operations.get(PROJECT_ID, operation.getName())).thenReturn(getRequest);
    when(getRequest.execute()).then(new OperationStatusAnswer(operation));
    OperationStatusChecker statusChecker =
        new OperationStatusChecker(sqlAdmin, PROJECT_ID, operation.getName(), statusCheckRetryer);

    OperationStatus status = statusChecker.checkStatus();

    verify(getRequest, IS_INVOKED_THREE_TIMES).execute();
    assertEquals(status, OperationStatus.DONE);
  }

  @Test
  public void checkStatus_whenSqlAdminApiThrowsException_thenExceptionIsThrown() throws Exception {
    when(operations.get(PROJECT_ID, operation.getName())).thenReturn(getRequest);
    IOException exception = new IOException("some message");
    when(getRequest.execute()).thenThrow(exception);
    OperationStatusChecker statusChecker =
        new OperationStatusChecker(sqlAdmin, PROJECT_ID, operation.getName(), statusCheckRetryer);

    try {
      statusChecker.checkStatus();
    } catch (CloudSqlException e) {
      assertNotNull(e);
    }
    verify(getRequest, IS_INVOKED_FIVE_TIMES).execute();
  }
}
