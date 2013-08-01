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
import com.google.api.services.fusiontables.model.Table;
import com.google.api.services.fusiontables.model.TableList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;

/**
 * Shows list of tables for the currently authorized user. Invokes Fusion Tables API with
 * Table.list. Displays each table with a link to either register or view a snippet.
 *
 * @author googletables-feedback@google.com (Anno Langen)
 */
public class ShowTablesServlet extends HttpServlet {

  public static final String URI = "show_tables";

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    PrintWriter out = resp.getWriter();
    OAuth2Tokens tokens = OAuth2Tokens.getSessionTokens(req);
    Fusiontables apiHandle = FusionTablesAccessor.getFusiontables(tokens);
    TableList tableList = apiHandle.table().list().execute();

    HashSet<String> registeredTables = new HashSet<String>();
    for (TableData tableData : TableStore.THE_ONE.getAll()) {
      registeredTables.add(tableData.id);
    }

    out.println("<html><head><title>My Fusion Tables</title>");
    out.println("<link rel='stylesheet' type='text/css' href='style.css'>");
    out.println("<script type='text/javascript' src='godocs.js'></script></head>");
    out.println("<body style='max-width:1170'><h2>Fusion Tables Listing</h2>");
    out.println(
        "Once registered, you and the rest of the world can see a 10 row snippet of the table.");
    out.println("<a href='https://www.google.com/accounts/IssuedAuthSubTokens'>"
        + "Manage authorized websites</a> to revoke this permission.<p>");
    if (tableList != null && tableList.size() > 0) {
      out.println("<table>");
      for (Table table : tableList.getItems()) {
        boolean isRegistered = registeredTables.contains(table.getTableId());
        out.print(
            "<tr><td>" + table.getName()
                + "</td><td><a href='"
                + (isRegistered ? SnippetServlet.URI : AddTableServlet.URI)
                + "?table=" + table.getTableId() + "&title=" + table.getName() + "'>"
                + (isRegistered ? "View Snippet" : "Register")
                + "</a></td></tr>");
      }
      out.println("</table>");
    }
    out.println("</body></html>");
  }
}
