<%--
  Created by IntelliJ IDEA.
  User: yhu
  Date: July 27, 2009
  Time: 10:55:42 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>

                       All Books
                      <g:each in="${books}" status="i" var="book">
                          <a class="rednav"  href="/gsword/bible/readgen/${book.initials}" title="${book.name}" >${book.name} |</a>
                      </g:each>
