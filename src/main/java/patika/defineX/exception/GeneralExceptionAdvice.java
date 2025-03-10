package patika.defineX.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import patika.defineX.exception.custom.DepartmentNotFoundException;
import patika.defineX.exception.custom.ProjectNotFoundException;
import patika.defineX.exception.custom.StatusChangeException;
import patika.defineX.exception.custom.UserNotFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GeneralExceptionAdvice extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest req) {
        return new ResponseEntity<>(new ErrorResponse(
                ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, System.currentTimeMillis(), req.getRequestURI()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex, HttpServletRequest req) {
        return new ResponseEntity<>(new ErrorResponse(
                ex.getMessage(), HttpStatus.NOT_FOUND, System.currentTimeMillis(), req.getRequestURI()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DepartmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDepartmentNotFoundException(DepartmentNotFoundException ex, HttpServletRequest req) {
        return new ResponseEntity<>(new ErrorResponse(
                ex.getMessage(), HttpStatus.NOT_FOUND, System.currentTimeMillis(), req.getRequestURI()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProjectNotFoundException(ProjectNotFoundException ex, HttpServletRequest req) {
        return new ResponseEntity<>(new ErrorResponse(
                ex.getMessage(), HttpStatus.NOT_FOUND, System.currentTimeMillis(), req.getRequestURI()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(StatusChangeException.class)
    public ResponseEntity<ErrorResponse> handleProjectNotFoundException(StatusChangeException ex, HttpServletRequest req) {
        return new ResponseEntity<>(new ErrorResponse(
                ex.getMessage(), HttpStatus.BAD_REQUEST, System.currentTimeMillis(), req.getRequestURI()),
                HttpStatus.BAD_REQUEST);
    }
}
