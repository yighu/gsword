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
                <g:textField name="keyp" />
            </td>
    
          <td>
<g:message code="srange" />          </td>
          <td>

              <g:select name="dictionariesp" id="dictionariesp"  class ='dictionaries' noSelection="['easton':'Eastons Bible Dictionary']"
                        from="${dictionaries}" value="name" optionKey="initials" optionValue="name" onchange="searchDictionaryp(); "/>

               
          </td>
      </tr>
     <tr >
         <td>
 <button type="button" id="adsearchbut" onclick="searchDictionaryp();"><g:message code="search" /></button>         </td>
       
     </tr>

    <td valign="top" width="100%" height="800">

<div id="display_dictp" name="display_dictp"/>
      <br/>

  </table>

  </body>
</html>
