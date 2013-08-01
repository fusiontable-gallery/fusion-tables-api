// Copyright 2011 Google Inc. All Rights Reserved.

package com.googlecodesamples;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.CredentialRefreshListener;
import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serializable;

/**
 * Holds tokens and the expiration of the access token for OAuth2.
 *
 * @author googletables-feedback@google.com (Anno Langen)
 */
public class OAuth2Tokens implements Serializable, CredentialRefreshListener {

  /**
   * Attribute name used in request or session scope.
   */
  private static final String OAUTH_2_TOKENS = "oauth2_tokens";
  
  public final String refresh;
  public String access;
  public long expiration;

  public static OAuth2Tokens getRequestTokens(HttpServletRequest req) {
    return (OAuth2Tokens) req.getAttribute(OAUTH_2_TOKENS);
  }

  public static OAuth2Tokens getSessionTokens(HttpServletRequest req) {
    HttpSession session = req.getSession(false);
    return session == null ? null : (OAuth2Tokens) session.getAttribute(OAUTH_2_TOKENS);
  }

  public OAuth2Tokens(String access, String refresh, long expiration) {
    this.access = access;
    this.refresh = refresh;
    this.expiration = expiration;
  }

  public OAuth2Tokens(TokenResponse resp) {
    this(resp.getAccessToken(), resp.getRefreshToken(),
        getExpirationMillis(resp.getExpiresInSeconds()));
  }

  public void setForRequest(HttpServletRequest req) {
    req.setAttribute(OAUTH_2_TOKENS, this);
  }

  public void setForSession(HttpServletRequest req) {
    req.getSession(true).setAttribute(OAUTH_2_TOKENS, this);
  }

  public TokenResponse toTokenResponse() {
    return new TokenResponse()
        .setAccessToken(access)
        .setRefreshToken(refresh)
        .setExpiresInSeconds((expiration - System.currentTimeMillis()) / 1000);
  }

  private static long getExpirationMillis(long expiresInSeconds) {
    return System.currentTimeMillis() + expiresInSeconds * 1000;
  }

  @Override
  public void onTokenResponse(Credential credential, TokenResponse tokenResponse)
      throws IOException {
    access = tokenResponse.getAccessToken();
    expiration = getExpirationMillis(tokenResponse.getExpiresInSeconds());
  }

  @Override
  public void onTokenErrorResponse(Credential credential, TokenErrorResponse errorResponse)
      throws IOException {
    throw new SecurityException(
        errorResponse.getError() + ": " + errorResponse.getErrorDescription());
  }
}
