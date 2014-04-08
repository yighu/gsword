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



    <title>Help</title></head>
  <body>
<h1>How to Use the Online Bible Studio</h1>
  <p>
      The Online Bible Studio contains functions from simple Bible search to advanced research.  
  </p>
  <p>
      For basic search,
      <ul>input your key word, such as "Love" into the "Search Words" box and</ul>
  <ul>
     select a Bible version from the "Search Bible" drop down list
  </ul>
  <ul>
      the click the "Lookup button on the right of the "Search Bible" dropdown list
  </ul>
  
  select a Bible version
  </p>
    <table>
        <tr>
  <td>

              Include this phrase
  </td>
            <td>
                <g:textField name="phrase" />
            </td>
        </tr>
       <tr>
           <td>
               Includes these words
           </td>
           <td>
               <g:textField name="inwords"/>
           </td>
       </tr>
      <tr>
           <td>
               Excludes all these words
           </td>
           <td>
               <g:textField name="exwords"/>
           </td>
       </tr>
      <tr>
           <td>
               Something like this spelling
           </td>
           <td>
               <g:textField name="seems"/>
           </td>
       </tr>
      <tr>
           <td>
               Includes words start with
           </td>
           <td>
               <g:textField name="starts"/>
           </td>
       </tr>
    </table>
  <table>
      <tr>
          <td>
              Search Range
          </td>
          <td>
              <%
                   def books = org.crosswire.bibledesktop.book.Msg.PRESETS.toString().split("\\|")
              %>
            <g:select name="range"
                           from="${books}"/>
               
          </td>
      </tr>
     
  </table>
  <button type="button" id="adsearchbut" onclick="doSearch();">Search</button>
  </body>
</html>
