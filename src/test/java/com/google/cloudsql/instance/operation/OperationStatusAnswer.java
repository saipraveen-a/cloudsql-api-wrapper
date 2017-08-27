package com.google.cloudsql.instance.operation;

import com.google.api.services.sqladmin.model.Operation;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class OperationStatusAnswer implements Answer<Operation> {
  private static final int TOTAL_EXPECTED_INVOCATIONS = 3;

  private Operation operation;
  private int count = 1;

  public OperationStatusAnswer(Operation operation) {
    this.operation = operation;
  }

  @Override
  public Operation answer(InvocationOnMock invocation) throws Throwable {
    if (count == TOTAL_EXPECTED_INVOCATIONS) {
      return operationDone(operation);
    }
    count = count + 1;
    return operationInProgress(operation);
  }

  private Operation operationInProgress(Operation operation) {
    operation.setStatus("RUNNING");
    return operation;
  }

  private Operation operationDone(Operation operation) {
    operation.setStatus("DONE");
    return operation;
  }
}
