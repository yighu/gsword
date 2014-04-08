<%--
  Created by IntelliJ IDEA.
  User: yhu
  Date: Jan 7, 2010
  Time: 10:55:42 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
                    <g:if test="${book}">
                   ${book.name}<br/>
                       </g:if>

                   <table>


                       <%
                         int i=-1
                       %>

                      <g:each in="${dictionaries}" status="j" var="dictionary">
                       <%
                         if (i<5){

                       %>
                        <%
                          if (i==-1){
                         

                        %>

                        <tr>
                        <%
                           
                              }

                        %>
                        <td>
                         <a class="rednav"  href="/gsword/gbook/searchdics/${dictionary.initials}" title="${dictionary.name}" >${dictionary.name}</a> &ensp;
                       </td>
                         <%
                         i++
                           }else {

                           i=0
                           %>
                             </tr>
                        <tr>
                             <td>


                             <a class="rednav"  href="/gsword/gbook/searchdics/${dictionary.initials}" title="${dictionary.name}" >${dictionary.name}</a> &ensp;
                        </td>
                           <%

                           }
                         %>

                      </g:each>
                     </tr> 
                     </table>
