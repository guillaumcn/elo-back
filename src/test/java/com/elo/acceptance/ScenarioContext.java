package com.elo.acceptance;

import io.cucumber.spring.ScenarioScope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ScenarioScope
public class ScenarioContext {

    private ResponseEntity<Map> response;
    private ResponseEntity<List> listResponse;
    private String authToken;

    public ResponseEntity<Map> getResponse() {
        return response;
    }

    public void setResponse(ResponseEntity<Map> response) {
        this.response = response;
        this.listResponse = null;
    }

    public ResponseEntity<List> getListResponse() {
        return listResponse;
    }

    public void setListResponse(ResponseEntity<List> listResponse) {
        this.listResponse = listResponse;
        this.response = null;
    }

    public int getLastStatusCode() {
        if (response != null) return response.getStatusCode().value();
        if (listResponse != null) return listResponse.getStatusCode().value();
        throw new IllegalStateException("No response stored in ScenarioContext");
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}
