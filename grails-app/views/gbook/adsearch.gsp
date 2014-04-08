<%--
  Created by IntelliJ IDEA.
  User: Yiguang
  Date: Dec 29, 2008
  Time: 8:34:27 PM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
  <head>



    <title><g:message code="advancedsearch" /></title></head>
  <body>
<g:message code="detailsearch" />    <table>
        <tr>
  <td>

<g:message code="incphr" />  </td>
            <td>
                <g:textField name="phrase" />
            </td>
        </tr>
       <tr>
           <td>
<g:message code="incwrd" />           </td>
           <td>
               <g:textField name="inwords"/>
           </td>
       </tr>
      <tr>
           <td>
<g:message code="excwrd" />           </td>
           <td>
               <g:textField name="exwords"/>
           </td>
       </tr>
      <tr>
           <td>
<g:message code="lookslike" />           </td>
           <td>
               <g:textField name="seems"/>
           </td>
       </tr>
      <tr>
           <td>
<g:message code="starts" />           </td>
           <td>
               <g:textField name="starts"/>
           </td>
       </tr>
    </table>
  <table>
      <tr>
          <td>
<g:message code="srange" />          </td>
          <td>
              <%
                  // def books = org.crosswire.bibledesktop.book.Msg.PRESETS.toString().split("\\|")
              %>
            <g:select name="range"  onChange="custom();"
                           from="${books}"/>
               
          </td>
      </tr>
     <tr >
         <td>
 <button type="button" id="adsearchbut" onclick="doSearch();"><g:message code="search" /></button>         </td>
         <td id="xcustomrange" name="xcustomrange" style="visibility:hidden;display:none;">
         <g:textField id="customrange" name="customrange" />
         </td>
     </tr>
  </table>

  </body>
</html>
