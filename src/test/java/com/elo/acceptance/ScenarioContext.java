package com.elo.acceptance;

import io.cucumber.spring.ScenarioScope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ScenarioScope
public class ScenarioContext {

    private ResponseEntity<Map> response;
    private String authToken;

    public ResponseEntity<Map> getResponse() {
        return response;
    }

    public void setResponse(ResponseEntity<Map> response) {
        this.response = response;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}
