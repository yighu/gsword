<%--
  Created by IntelliJ IDEA.
  User: yhu
  Date: July 27, 2009
  Time: 10:55:42 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<g:if test="${bible}">
  <a class="rednav"  href="/gsword/bible/cmnt" title="All Commentaries" ><g:message code="allcomnts"/>|</a>
                   
                    <a class="rednav"  href="/gsword/bible/cmnt/${book.initials}" title="${book.name}" >${book.name}|</a>
                    <g:if test="${chapters}">
                     ${bible.shortname} Chapter 
                      <% for (int chapter=1;chapter<=chapters;chapter++) {%>

                          <a class="rednav"  href="/gsword/bible/cmnt/${book.initials}/${bible.key}/${chapter}" title="${chapter}" >${chapter} |</a>
                        <% } %>


                    </g:if>
</g:if>
                   
