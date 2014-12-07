package photogift.server.error;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpStatusCodeException;
import photogift.server.exception.AlreadyExistsException;
import photogift.server.exception.BadParameterException;
import photogift.server.exception.BadStateException;
import photogift.server.exception.NotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.capitalize;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON;

// Pour voir les exceptions déjà traitée DefaultHandlerExceptionResolver
@ControllerAdvice
class ApplicationExceptionHandler {

    private final static Logger LOG = LoggerFactory.getLogger(ApplicationExceptionHandler.class);

//    @Value("${showErrorDebugMessage}")
    private boolean showErrorDebugMessage = true;

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorInfo> genericHandler(Exception e) throws Exception {

        if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null)
            throw e;

        ErrorInfo errorInfo = new ErrorInfo();
        errorInfo.message = e.getMessage();
        errorInfo.debugMessage = getDebugMessage(e);
        errorInfo.status = BAD_REQUEST.value();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);

        return new ResponseEntity<ErrorInfo>(errorInfo, headers, BAD_REQUEST);
    }

    @ExceptionHandler(value = {NotFoundException.class, AlreadyExistsException.class, BadStateException.class, BadParameterException.class})
    public ResponseEntity<ErrorInfo> signErrorHandler(HttpServletRequest req, Exception e) {

        final HttpStatus status;

        if (e instanceof NotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (e instanceof AlreadyExistsException) {
            status = HttpStatus.CONFLICT;
        } else if (e instanceof BadStateException) {
            status = HttpStatus.BAD_REQUEST;
        } else if (e instanceof BadParameterException) {
            status = BAD_REQUEST;
        } else {
            status = INTERNAL_SERVER_ERROR;
        }

        return createResponse(e, e.getMessage(), status);
    }

    @ExceptionHandler(value = TypeMismatchException.class)
    public ResponseEntity<ErrorInfo> handleTypeMismatchException(TypeMismatchException e) {

        String message = "Failed to convert to required type : " + e.getRequiredType().getSimpleName();

        return createResponse(e, message, BAD_REQUEST);
    }

    /**
     * Les exceptions remontés par le client RestTemplate
     */
    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<ErrorInfo> handleHttpStatusCodeException(HttpStatusCodeException e) {

        return createResponse(e, e.getStatusText(), e.getStatusCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorInfo> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        return createResponse(e, "Validation Error", BAD_REQUEST, handleValidationFailure(e));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorInfo> handleConstraintViolationException(ConstraintViolationException e) {

        return createResponse(e, "Validation Error", BAD_REQUEST, handleConstraintViolation(e));
    }

    private ResponseEntity<ErrorInfo> createResponse(Exception e, String message, HttpStatus status ) {
        return createResponse(e, message, status, null);
    }

    private ResponseEntity<ErrorInfo> createResponse(Exception e, String message, HttpStatus status, ArrayList<ErrorInfo.Property> properties ) {

        ErrorInfo errorInfo = new ErrorInfo();
        errorInfo.message = message;
        errorInfo.debugMessage = getDebugMessage(e);
        errorInfo.status = status.value();
        errorInfo.properties = properties;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);

        return new ResponseEntity<ErrorInfo>(errorInfo, headers, status);
    }


    private ArrayList<ErrorInfo.Property> handleConstraintViolation(ConstraintViolationException e) {

        ArrayList<ErrorInfo.Property> properties = new ArrayList<ErrorInfo.Property>();

        Set<ConstraintViolation<?>> constraintViolations = e.getConstraintViolations();

        for (ConstraintViolation<?> violation : constraintViolations) {

            ErrorInfo.Property property = new ErrorInfo.Property();
            property.message = violation.getMessage();
            property.object = violation.getRootBean().getClass().getSimpleName();
            property.field = getPath(violation);
            properties.add(property);

        }

        return properties;
    }

    /**
     * En environnement hors prod, on affiche les messages de debuggage.
     */
    private String getDebugMessage(Throwable throwable) {

        String debugMessage = "";

        if (showErrorDebugMessage) {
            final StringWriter stackStrace = new StringWriter();
            throwable.printStackTrace(new PrintWriter(stackStrace));
            debugMessage = stackStrace.toString();
        }

        return debugMessage;
    }

    private ArrayList<ErrorInfo.Property> handleValidationFailure(MethodArgumentNotValidException exception) {

        List<ObjectError> bindingErrors = exception.getBindingResult().getAllErrors();

        ArrayList<ErrorInfo.Property> properties = new ArrayList<ErrorInfo.Property>();

        for (ObjectError error : bindingErrors) {

            ErrorInfo.Property property = new ErrorInfo.Property();
            property.message = error.getDefaultMessage();
            property.object = capitalize(error.getObjectName());


            if (error instanceof FieldError) {
                property.field = ((FieldError) error).getField();
            }

            properties.add(property);

        }

        return properties;
    }

    private String getPath(ConstraintViolation<?> violation) {
        String pathStr = "";
        Path path = violation.getPropertyPath();
        for (Path.Node node : path) {
            pathStr += node.getName() + ".";
        }
        // removing the trailing dot "."
        pathStr = pathStr.substring(0, pathStr.length() - 1);
        return pathStr;
    }

}