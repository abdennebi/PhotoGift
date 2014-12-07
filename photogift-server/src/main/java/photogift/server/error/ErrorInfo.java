package photogift.server.error;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;

public class ErrorInfo {

    public ErrorInfo() {
    }

    public ErrorInfo(int status, String code, String message, String debugMessage, ArrayList<Property> properties) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.debugMessage = debugMessage;
        this.properties = properties;
    }

    public ErrorInfo(int status, String code, String message, String debugMessage) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.debugMessage = debugMessage;
    }

    /**
     * Http status code.
     */
    public int status;

    /**
     * Code erreur spécifique SIGN. Non utilisé pour l'instant.
     */
    @JsonIgnore
    public String code;

    public String message;

    public String debugMessage;

    public ArrayList<Property> properties;

    public static class Property {

        public String object;

        public String message;

        public String field;
    }

}