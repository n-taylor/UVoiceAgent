package ute.webservice.voiceagent.exceptions;

/**
 * Throw this when the response from a webservice is not what was expected.
 */
public class InvalidResponseException extends Exception {
    public InvalidResponseException(String message){
        super(message);
    }
    public InvalidResponseException(){ super(); }
}
