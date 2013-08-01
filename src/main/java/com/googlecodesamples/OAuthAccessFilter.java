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

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;

/**
 * Obtains OAuth2 tokens, and saves them in session state. This filter implements all client
 * steps of the OAuth dance.
 *
 * <ul><li>On the first invocation, the session lacks OAuth2 tokens and is not the special URI
 * registered for OAuth2 callback. In that case the filter obtains a request token, constructs a
 * callback URL for the current request URL that includes an {@code oauth_token_secret} parameter,
 * and uses this callback in a redirect to the authorization URL.</li>
 *
 * <li>The second invocation is a redirect from the authorizer to the provided callback. Now the
 * session still lacks an access token, but there are two characteristic parameters: {@code
 * oauth_token_secret} and {@code oauth_token}. In this case we upgrade to an access token and save
 * it in session state.</li>
 *
 * <li>All subsequent invocations find the access token in their session state.</li></ul>
 *
 * See <a href="http://code.google.com/apis/accounts/docs/OAuth.html">OAuth for Web Applications</a>
 * for more information.
 *
 * @author googletables-feedback@google.com (Anno Langen)
 */
public class OAuthAccessFilter implements Filter {

  /**
   * Session attribute name for continuing with a successful authorization.
   */
  public static final String OAUTH_CONTINUE_URL = "oauth_continue_url";

  /**
   * Four constants from the web application client registration at
   * <a href='http://code.google.com/apis/console#access'>API console</a>.
   */
  private String clientId;
  private String clientSecret;
  private String scope;
  private String registeredCallbackUri;

  /** URL to redirect to when user declines to authorize this application. */
  private String nonConsentRedirect;

  public void init(FilterConfig config) throws ServletException {
    clientId = config.getInitParameter("client_id");
    clientSecret = config.getInitParameter("client_secret");
    registeredCallbackUri = config.getInitParameter("registered_callback_uri");
    scope = config.getInitParameter("oauth_scope");
    nonConsentRedirect = config.getInitParameter("user_consent_denied_redirect");
  }

  public void destroy() {
  }

  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
      throws ServletException, IOException {
    HttpServletRequest httpReq = (HttpServletRequest) req;
    HttpServletResponse httpResp = (HttpServletResponse) resp;

    if (!hasSessionOAuth2Tokens(httpReq)) {
      String fullRequestUrl = getFullRequestUrl(httpReq);
      if (!registeredCallbackUri.endsWith(httpReq.getRequestURI())) {
        // First invocation for this session. Save request URL and redirect to user consent flow
        httpReq.getSession(true).setAttribute(OAUTH_CONTINUE_URL, fullRequestUrl);
        AuthorizationCodeRequestUrl authorizationUrl = newFlow().newAuthorizationUrl();
        authorizationUrl.setRedirectUri(registeredCallbackUri);
        // This application provides public access to a snippet, even for private tables, when the
        // owner is "offline".
        authorizationUrl.set("access_type", "offline");
        httpResp.sendRedirect(authorizationUrl.build());
        return;
      }

      // Handling the registered OAuth2 callback. Check for valid consent code.
      AuthorizationCodeResponseUrl responseUrl = new AuthorizationCodeResponseUrl(fullRequestUrl);
      String code = responseUrl.getCode();
      if (responseUrl.getError() != null) {
        httpResp.sendRedirect(nonConsentRedirect);
        return;
      }
      if (code == null) {
        httpResp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.getWriter().print("Missing authorization code");
        return;
      }
      HttpSession session = httpReq.getSession(false);
      if (session == null || session.getAttribute(OAUTH_CONTINUE_URL) == null) {
        httpResp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.getWriter().print("Illegal OAuth2 state");
        return;
      }

      // We have a valid consent code. Now redeem the code for OAuth2 tokens.
      AuthorizationCodeTokenRequest tokenRequest = newFlow().newTokenRequest(code);
      TokenResponse response = tokenRequest.setRedirectUri(registeredCallbackUri).execute();

      // Save the tokens to the current session and redirect to the URL saved in the first step.
      new OAuth2Tokens(response).setForSession(httpReq);
      String continueUrl = (String) session.getAttribute(OAUTH_CONTINUE_URL);
      session.removeAttribute(OAUTH_CONTINUE_URL);
      httpResp.sendRedirect(continueUrl);
      return;
    }
    // OAuth2 tokens are stored in the session.
    chain.doFilter(req, resp);
  }

  private AuthorizationCodeFlow newFlow() {
    return new GoogleAuthorizationCodeFlow.Builder(new NetHttpTransport(), new JacksonFactory(),
        clientId, clientSecret, Collections.singleton(scope))
      //  .setCredentialStore(new MemoryCredentialStore())
        .build();
  }

  private static String getFullRequestUrl(HttpServletRequest httpReq) {
    StringBuffer urlBuffer = httpReq.getRequestURL();
    String queryParams = httpReq.getQueryString();
    if (queryParams != null) {
      urlBuffer.append('?').append(queryParams);
    }
    return urlBuffer.toString();
  }

  /**
   * Indicates whether the session contains an OAuth access token.
   */
  private static boolean hasSessionOAuth2Tokens(HttpServletRequest httpReq) {
    return OAuth2Tokens.getSessionTokens(httpReq) != null;
  }
}
