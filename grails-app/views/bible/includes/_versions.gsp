<%--
  Created by IntelliJ IDEA.
  User: yhu
  Date: July 27, 2009
  Time: 10:55:42 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<g:message code="searchbible" />
%{--<g:each in="${books}" status="j" var="book">
  <a class="rednav" href="/gsword/bible/read/${book.initials}" title="${book.name}">${book.name} |</a>
</g:each>--}%

<table>

  <%
    int i = -1
  %>

  <g:each in="${books}" status="x" var="book">
    <%
      if (i < 2) {

    %>
    <%
        if (i == -1) {


    %>

    <tr>
      <%

          }

      %>
      <td>
        <a class="rednav" href="/gsword/bible/read/${book.initials}" title="${book.name}">${book.name}</a> &ensp;

      </td>
      <%
          i++
        } else {

          i = 0
      %>
    </tr>
    <tr>
    <td>


 <a class="rednav"  href="/gsword/bible/read/${book.initials}" title="${book.name}" >${book.name}</a> &ensp;
                        </td>
    <%

      }
    %>

  </g:each>
</tr>
</table>
