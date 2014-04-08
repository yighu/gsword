<%--
  Created by IntelliJ IDEA.
  User: yhu
  Date: July 27, 2009
  Time: 10:55:42 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
    <g:if test="${book}">
      <a class="rednav" href="/gsword/bible/cmnt" title="All Commentaries"><g:message code="allcomnts"/>|</a>

      <a class="rednav" href="/gsword/bible/cmnt/${book.initials}" title="${book.name}">${book.name}|</a>
      <g:if test="${bible}">

        <a class="rednav" href="/gsword/bible/cmnt/${book.initials}/${bible.key}" title="${bible.shortname}">${bible.shortname}|</a>
   <g:if test="${chapters}">
                     Chapter 
                      <% for (int chapter=1;chapter<=chapters;chapter++) {%>
                          <g:if test="${chap}">
                            <g:if test="${Integer.parseInt(chap)==chapter}">
                            ${chapter} |  
                            </g:if>
                            <g:else>
                              <a class="rednav"  href="/gsword/bible/cmnt/${book.initials}/${bible.key}/${chapter}" title="${chapter}" >${chapter} |</a>

                            </g:else>

                            </g:if>

                        <% } %>


                    </g:if>

        <g:if test="${chap}">
        <br> Total ${verses} verses in Chapter
          <a class="rednav" href="/gsword/bible/cmnt/${book.initials}/${bible.key}/${chap}" title="${chap}">${chap}:</a>
                                                                                                                             
          <g:if test="${verses}">
            <% for (int verse = 1; verse <= verses; verse++) { %>

            <a class="rednav" href="/gsword/bible/cmnt/${book.initials}/${bible.key}/${chap}/${verse}" title="${verse}">${verse} |</a>
            <% } %>

          </g:if>
        </g:if>

      </g:if>
    </g:if>

