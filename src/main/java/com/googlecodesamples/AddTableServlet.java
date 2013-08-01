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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

/**
 * Registers the table indicated by the query parameter with the current OAuth access token.
 * Redirects to show_tables.
 *
 * @author googletables-feedback@google.com (Anno Langen)
 */

public class AddTableServlet extends HttpServlet {

  public static final String URI = "add_table";

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    String table = req.getParameter("table");
    if (table == null) {
      resp.sendError(SC_BAD_REQUEST, "missing parameter: table");
      return;
    }
    TableStore.THE_ONE.store(new TableData(table, req.getParameter("title"), OAuth2Tokens.getSessionTokens(req)));
    resp.sendRedirect(ShowTablesServlet.URI);
  }
}
