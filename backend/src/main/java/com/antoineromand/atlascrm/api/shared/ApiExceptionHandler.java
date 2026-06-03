package com.antoineromand.atlascrm.api.shared;

import com.antoineromand.atlascrm.authentication.application.exceptions.AuthenticationException;
import com.antoineromand.atlascrm.client.application.exceptions.ClientCreationException;
import com.antoineromand.atlascrm.client.application.exceptions.ClientNotFoundException;
import com.antoineromand.atlascrm.mission.application.exceptions.MissionCreationException;
import com.antoineromand.atlascrm.mission.application.exceptions.MissionNotFoundException;
import com.antoineromand.atlascrm.mission.application.exceptions.MissionUpdateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    Map<String, Object> details = new LinkedHashMap<>();
        ex.getBindingResult()
        .getFieldErrors()
        .forEach(
            error ->
                details.putIfAbsent(
                    error.getField(),
                    error.getDefaultMessage()));

    return ResponseEntity.badRequest()
        .body(new ApiErrorResponse("INVALID_PARAMETERS", "Some parameters are invalid.", 400, details));
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<ApiErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiErrorResponse("MISSING_HEADER", ex.getMessage(), 400, null));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
    return ResponseEntity.badRequest()
        .body(new ApiErrorResponse("INVALID_PARAMETERS", ex.getMessage(), 400, null));
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
      AuthenticationException ex) {
    HttpStatus status =
        switch (ex.getCode()) {
          case "EMAIL_ALREADY_USED", "DUPLICATED_CREDENTIALS" -> HttpStatus.CONFLICT;
          case "CREDENTIALS_NOT_ACTIVE" -> HttpStatus.FORBIDDEN;
          case "CREDENTIALS_NOT_FOUND", "ACCOUNT_NOT_FOUND" -> HttpStatus.NOT_FOUND;
          case "INVALID_TOKEN", "TOKEN_REVOKED", "INVALID_CREDENTIALS", "EMAIL_NOT_VERIFIED" ->
              HttpStatus.UNAUTHORIZED;
          default -> HttpStatus.BAD_REQUEST;
        };

    return ResponseEntity.status(status)
        .body(new ApiErrorResponse(ex.getCode(), ex.getMessage(), status.value(), null));
  }

  @ExceptionHandler(MissionCreationException.class)
  public ResponseEntity<ApiErrorResponse> handleMissionCreationException(
      MissionCreationException ex) {
    HttpStatus status = "ACCOUNT_NOT_FOUND".equals(ex.getCode()) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

    return ResponseEntity.status(status)
        .body(new ApiErrorResponse(ex.getCode(), ex.getMessage(), status.value(), null));
  }

  @ExceptionHandler(MissionNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleMissionNotFoundException(
      MissionNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiErrorResponse(ex.getCode(), ex.getMessage(), 404, null));
  }

  @ExceptionHandler(MissionUpdateException.class)
  public ResponseEntity<ApiErrorResponse> handleMissionUpdateException(MissionUpdateException ex) {
    HttpStatus status = "MISSION_NOT_FOUND".equals(ex.getCode()) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

    return ResponseEntity.status(status)
        .body(new ApiErrorResponse(ex.getCode(), ex.getMessage(), status.value(), null));
  }

  @ExceptionHandler(ClientCreationException.class)
  public ResponseEntity<ApiErrorResponse> handleClientCreationException(ClientCreationException ex) {
    HttpStatus status = "ACCOUNT_NOT_FOUND".equals(ex.getCode()) ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;

    return ResponseEntity.status(status)
        .body(new ApiErrorResponse(ex.getCode(), ex.getMessage(), status.value(), null));
  }

  @ExceptionHandler(ClientNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleClientNotFoundException(ClientNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiErrorResponse(ex.getCode(), ex.getMessage(), 404, null));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            new ApiErrorResponse(
                "INTERNAL_SERVER_ERROR",
                ex.getMessage() != null ? ex.getMessage() : "The server encountered an internal error.",
                500,
                null));
  }
}
