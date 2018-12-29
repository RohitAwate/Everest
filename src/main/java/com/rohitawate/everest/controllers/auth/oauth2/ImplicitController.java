package com.rohitawate.everest.controllers.auth.oauth2;

import com.rohitawate.everest.auth.oauth2.tokens.OAuth2Token;
import com.rohitawate.everest.state.auth.ImplicitState;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class ImplicitController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setAccessToken(OAuth2Token token) {

    }

    public ImplicitState getState() {
        return null;
    }
}
