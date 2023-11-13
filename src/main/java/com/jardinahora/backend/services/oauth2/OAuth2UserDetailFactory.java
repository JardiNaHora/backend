package com.jardinahora.backend.services.oauth2;

import com.jardinahora.backend.exceptions.BaseException;
import com.jardinahora.backend.models.Provider;

import java.util.Map;

public class OAuth2UserDetailFactory {

    public static OAuth2UserDetails  getOAuth2UserDetails(String registrationId, Map<String, Object> attributes) {
        if(registrationId.equals(Provider.google.name())){
            return new OAuth2GoogleUser(attributes);
        } else if (registrationId.equals(Provider.github.name())) {
            return new OAuth2GithubUser(attributes);
        } else {
            throw new BaseException("400", "Desculpe! O login com " + registrationId + " não é válido!");
        }
    }

}
