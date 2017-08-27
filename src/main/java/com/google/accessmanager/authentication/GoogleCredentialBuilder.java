package com.google.accessmanager.authentication;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sqladmin.SQLAdminScopes;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.Set;

public final class GoogleCredentialBuilder {
  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();

  private GoogleCredentialBuilder() {}

  public static Credential getCredentials(String keyFile, String serviceAccount) {
    File p12file = new File(keyFile);
    if (!p12file.exists() || !p12file.isFile()) {
      URL fileUrl = GoogleCredentialBuilder.class.getResource(keyFile);
      try {
        p12file = new File(fileUrl.toURI());
      } catch (URISyntaxException e) {
        throw new GoogleAuthorizationException("Invalid credentials", e);
      }
    }

    Set<String> scopes = new HashSet<>();
    scopes.addAll(SQLAdminScopes.all());

    try {
      return new GoogleCredential.Builder().setTransport(HTTP_TRANSPORT)
          .setJsonFactory(JSON_FACTORY)
          .setServiceAccountId(
              serviceAccount)
          .setServiceAccountScopes(scopes)
          .setServiceAccountPrivateKeyFromP12File(p12file)
          .build();
    } catch (GeneralSecurityException | IOException e) {
      throw new GoogleAuthorizationException("Unable to authorize at the moment", e);
    }
  }
}
