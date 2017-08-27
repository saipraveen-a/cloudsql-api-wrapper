package com.google.cloudsql.instance.operation;

import static java.lang.String.format;

import com.google.cloudsql.exception.CloudSqlException;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.google.api.services.sqladmin.SQLAdmin;
import com.google.api.services.sqladmin.model.Operation;
import com.google.api.services.sqladmin.model.OperationError;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class OperationStatusChecker implements Callable<OperationStatus> {
  private static final Logger LOGGER = LoggerFactory.getLogger(OperationStatusChecker.class);
  private static final long STOP_DELAY_IN_MINUTES = 5L;

  private String projectId;
  private String operationName;
  private SQLAdmin sqlAdmin;
  private OperationStatusRetryerFactory<OperationStatus> retryer;

  public OperationStatusChecker(
      SQLAdmin sqlAdmin,
      String projectId,
      String operationName,
      OperationStatusRetryerFactory<OperationStatus> retryer) {
    this.projectId = projectId;
    this.sqlAdmin = sqlAdmin;
    this.operationName = operationName;
    this.retryer = retryer;
  }

  @Override
  public OperationStatus call() {
    try {
      Operation operation = sqlAdmin.operations().get(projectId, operationName).execute();
      LOGGER.info("Operation status for operation: " + operationName + " is " + operation.getStatus());

      if (operationHasErrors(operation)) {
        List<String> operationErrors = getOperationErrors(operation);
        throw new CloudSqlException(
            format(
                "Operation failed with %d errors : %s", operationErrors.size(), StringUtils.join(
                    operationErrors, ",")));
      }
      return OperationStatus.valueOf(operation.getStatus());
    } catch (IOException e) {
      throw new CloudSqlException("Failed to get operation details: " + operationName, e);
    }
  }

  private List<String> getOperationErrors(Operation operation) {
    List<String> errors = new ArrayList<>();
    if (operationHasErrors(operation)) {
      for (OperationError operationError : operation.getError().getErrors()) {
        errors.add(operationError.getMessage());
      }
    }
    return errors;
  }

  private boolean operationHasErrors(Operation operation) {
    return operation.getError() != null && operation.getError().getErrors() != null && !operation.getError()
        .getErrors()
        .isEmpty();
  }

  public OperationStatus checkStatus() {
    try {
      return getStatusRetryer().call(this);
    } catch (ExecutionException | RetryException e) {
      if (e.getCause() instanceof CloudSqlException) {
        throw new CloudSqlException(
            format("Operation failure: %s errors : %s", operationName, e.getCause().getMessage()), e);
      }
      throw new CloudSqlException("Failed to check status for operation: " + operationName, e);
    }
  }

  private Retryer<OperationStatus> getStatusRetryer() {
    return retryer.getStatusCheckRetryer(STOP_DELAY_IN_MINUTES);
  }
}
