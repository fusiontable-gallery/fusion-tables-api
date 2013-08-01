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

import com.google.api.services.fusiontables.Fusiontables;
import com.google.api.services.fusiontables.model.Sqlresponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Shows a snippet of up to 10 rows from the indicated table. Requires {@code RegisteredTableFilter}
 * to ensure that the table parameter has registered OAuth credentials. Invokes Fusion Tables API
 * with SQL {@code select * from ... limit 10}.
 *
 * @author googletables-feedback@google.com (Anno Langen)
 */
public class SnippetServlet extends HttpServlet {

  public static final String URI = "snippet";

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    OAuth2Tokens tokens = OAuth2Tokens.getRequestTokens(req);
    Fusiontables apiHandle = FusionTablesAccessor.getFusiontables(tokens);
    String statement = "select * from " + req.getParameter("table") + " limit 10";
    Sqlresponse body = apiHandle.query().sql(statement).execute();
    if (body != null) {
      PrintWriter out = resp.getWriter();
      out.println("<table border=1 cellspacing=0 cellpadding=3 bordercolor=lightgrey>");
      out.append("<tr style='background:#e0e0e0'>");
      for (String column : body.getColumns()) {
        out.append("<th>").append(column).append("</th>");
      }
      out.println("</tr>");
      List<List<Object>> rows = body.getRows();
      if (rows != null) {
        for (List<Object> row : rows) {
          out.append("<tr>");
          for (Object cell : row) {
            out.append("<td>").append(String.valueOf(cell)).append("</td>");
          }
          out.println("</tr>");
        }
      }
      out.println("</table>");
    }
  }
}
