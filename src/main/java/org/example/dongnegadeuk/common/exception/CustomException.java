package org.example.dongnegadeuk.common.exception;

import lombok.Getter;
import org.example.dongnegadeuk.common.exception.model.BaseErrorCode;

@Getter
public class CustomException extends RuntimeException {

  private final BaseErrorCode errorCode;

  public CustomException(BaseErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}