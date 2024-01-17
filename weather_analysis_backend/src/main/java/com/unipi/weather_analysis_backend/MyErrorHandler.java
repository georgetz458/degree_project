package com.unipi.weather_analysis_backend;

import com.google.firebase.auth.FirebaseAuthException;
import org.json.JSONObject;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Map;
//διαχείριση σφαλμάτων
public class MyErrorHandler  {


    private static void handleError(Throwable t) {

        System.out.println(t.getMessage());
    }
    public static ResponseEntity<Map<String, Object>> handleException(Exception e){
        if(e instanceof DataIntegrityViolationException){
            return handleDataIntegrityViolationException((DataIntegrityViolationException) e);
        }
        else if(e instanceof BadCredentialsException){
            return  handleBadCredentialsException((BadCredentialsException) e);
        }
        else {
            handleError(e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

    }
    private static ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(DataIntegrityViolationException e){

        JSONObject errorBody = new JSONObject();
        String message = e.getMostSpecificCause().getMessage();
        String[] parts = message.split("\n");
        String detailsMessage = parts[1];
        String errorMessage = detailsMessage.substring(detailsMessage.lastIndexOf(":"));
        errorBody.put("error", errorMessage);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody.toMap());
    }
    private static ResponseEntity<Map<String, Object>> handleBadCredentialsException(BadCredentialsException e){
        JSONObject object = new JSONObject();
        object.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(object.toMap());
    }
    private static ResponseEntity<Map<String, Object>> handleFirebaseAuthException(FirebaseAuthException e){
        JSONObject object = new JSONObject();
        object.put("error",e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(object.toMap());
    }
    //private constructor
    private MyErrorHandler(){}
}
