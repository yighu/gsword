<%--
  Created by IntelliJ IDEA.
  User: yhu
  Date: July 27, 2009
  Time: 10:55:42 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
                    <a class="rednav"  href="/gsword/bible/read" title="All Bibles" ><g:message code="searchbible" />|</a>
                    <g:if test="${book}">
                   ${book.name}<br/>

                   <table>


                       <%
                         int i=-1
                       %>

                      <g:each in="${bibles}" status="j" var="bible">
                       <%
                         if (i<21){

                       %>
                        <%
                          if (i==-1){
                         

                        %>

                        <tr>
                        <%
                           
                              }

                        %>
                        <td>
                         <a class="rednav"  href="/gsword/bible/read/${book.initials}/${bible.key}" title="${bible.shortname}" >${bible.shortname}</a> &ensp;
                       </td>
                         <%
                         i++
                           }else {

                           i=0
                           %>
                             </tr>
                        <tr>
                             <td>


                             <a class="rednav"  href="/gsword/bible/read/${book.initials}/${bible.key}" title="${bible.shortname}" >${bible.shortname}</a> &ensp;
                        </td>
                           <%

                           }
                         %>

                      </g:each>
                     </tr> 
                     </table>
                       </g:if>
