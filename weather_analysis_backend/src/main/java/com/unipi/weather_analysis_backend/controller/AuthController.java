package com.unipi.weather_analysis_backend.controller;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.nimbusds.jose.JOSEException;
import com.unipi.weather_analysis_backend.MyErrorHandler;
import com.unipi.weather_analysis_backend.Repository.RoleRepository;
import com.unipi.weather_analysis_backend.model.entity.Role;
import com.unipi.weather_analysis_backend.model.record.LogInRequest;
import com.unipi.weather_analysis_backend.Repository.UserRepository;
import com.unipi.weather_analysis_backend.model.entity.User;
import com.unipi.weather_analysis_backend.model.record.RegisterRequest;
import org.json.JSONObject;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {
    private final UserRepository userRepository;

    private final RoleRepository roleRepository;







    public AuthController( UserRepository userRepository, RoleRepository roleRepository) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }
    //εισαγωγή ρόλου στα επακόλουθα token της firebase
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LogInRequest logInRequest) throws AuthenticationException, ParseException, JOSEException {


        try {
            String token = logInRequest.token();
            FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(token);
            String userId = firebaseToken.getUid();
            String role;
            //ανάκτηση του ρόλου
            if(userRepository.findByUserId(userId).isPresent()){

                role = userRepository.findByUserId(userId).get().getRole().getRole();
            }else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            //εισαγωγή ρόλου στο scope του token
            Map<String, Object> claims = new HashMap<>();
            claims.put("scope", role);
            FirebaseAuth.getInstance().setCustomUserClaims(userId, claims);
            JSONObject response = new JSONObject();
            response.put("Result", "Success");
            return ResponseEntity.status(HttpStatus.OK).body(response.toMap());
        }catch (DataIntegrityViolationException e){//Διαχείριση σφάλματος σε περίπτωση που δοθεί ήδη υπάρχων χρήστης
            System.out.println("DataIntegrityViolationException");
            return MyErrorHandler.handleException(e);
        } catch (FirebaseAuthException e) {
            System.out.println("FirebaseAuthException");

            return MyErrorHandler.handleException(e);
        }

    }
    //εισαγωγή χρήστη USER στη βάση και εισαγωγή ρόλου στα επακόλουθα tokens της firebase
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest registerRequest){
        try{
            String token = registerRequest.token();
            FirebaseToken firebaseToken = FirebaseAuth.getInstance().verifyIdToken(token);
            String userId = firebaseToken.getUid();
            //εισαγωγή ρόλου στο scope του token
            Map<String, Object> claims = new HashMap<>();
            claims.put("scope", registerRequest.role());
            FirebaseAuth.getInstance().setCustomUserClaims(userId, claims);
            //εισαγωγή ρόλου του χρήστη στη βάση δεδομένων
            Role role = roleRepository.findByRole(registerRequest.role());
            User user = new User();
            user.setUserId(userId);
            user.setRole(role);
            userRepository.save(user);
            JSONObject response = new JSONObject();
            response.put("Result", "Success");
            return  ResponseEntity.status(HttpStatus.OK).body(response.toMap());
        }catch (DataIntegrityViolationException e){//Διαχείριση σφάλματος σε περίπτωση που δοθεί ήδη υπάρχων χρήστης
            return MyErrorHandler.handleException(e);
        } catch (FirebaseAuthException e) {

            return MyErrorHandler.handleException(e);
        }


    }



}