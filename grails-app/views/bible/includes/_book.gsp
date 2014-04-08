<%--
  Created by IntelliJ IDEA.
  User: yhu
  Date: July 27, 2009
  Time: 10:55:42 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<g:if test="${bible}">
  <a class="rednav"  href="/gsword/bible/read" title="All Bibles" ><g:message code="searchbible" />-></a>
                   
                    <a class="rednav"  href="/gsword/bible/read/${book.initials}" title="${book.name}" >${book.name}-></a>
                    <g:if test="${chapters}">
                     ${bible.shortname} <g:message code="chapters"/>
                      <% for (int chapter=1;chapter<=chapters;chapter++) {%>

                          <a class="rednav"  href="/gsword/bible/read/${book.initials}/${bible.key}/${chapter}" title="${chapter}" >${chapter},</a>
                        <% } %>


                    </g:if>
</g:if>
                   
