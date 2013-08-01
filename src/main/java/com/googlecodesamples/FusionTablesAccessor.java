/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecodesamples;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.http.HttpExecuteInterceptor;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.fusiontables.Fusiontables;

import java.io.IOException;
import java.util.Collections;

/**
 * Returns Fusiontables API handle for {@link OAuth2Tokens}.
 *
 * @author googletables-feedback@google.com (Anno Langen)
 */
public class FusionTablesAccessor {

  public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  public static final JacksonFactory JSON_FACTORY = new JacksonFactory();

  // Refresh tokens require a non-null client authentication instance.
  public static final HttpExecuteInterceptor NOOP_CLIENT_AUTHENTICATION =
      new HttpExecuteInterceptor() {
    public void intercept(HttpRequest request) throws IOException {
    }
  };

  /**
   * Returns Fusiontables API handle for {@link OAuth2Tokens}.
   */
  public static Fusiontables getFusiontables(OAuth2Tokens tokens) throws IOException {
    return new Fusiontables.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential(tokens))
        // Shuts up warning from AbstractGoogleClient.
        .setApplicationName("snippet")
        .build();
  }

  /**
   * Builds a Credential for use with the Fusiontables client API from {@link OAuth2Tokens}.
   */
  // This is non-obvious because the API is tailored for online access, where the user's
  // authorization is tied to the session and a new access token can be fetched with redirects. The
  // snippet application requires ``offline'' access where a table owner can sanction
  // unauthenticated access to a snippet of their private table. Offline access is evidently rare
  // for web applications and explains the need for registering the OAuth2Tokens instance as a
  // refresh listener. When the access token is about to expire a new refresh token is requested and
  // made available through a listener interface.
  private static Credential getCredential(OAuth2Tokens tokens) throws IOException {
    return newCredentialBuilder(tokens).build().setFromTokenResponse(tokens.toTokenResponse());
  }

  private static Credential.Builder newCredentialBuilder(final OAuth2Tokens tokens) {
    return new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
        .setTransport(HTTP_TRANSPORT)
        .setJsonFactory(JSON_FACTORY)
        .setClientAuthentication(NOOP_CLIENT_AUTHENTICATION)
        .setRefreshListeners(Collections.<CredentialRefreshListener>singleton(tokens))
        .setTokenServerEncodedUrl(GoogleOAuthConstants.TOKEN_SERVER_URL);
  }
}
