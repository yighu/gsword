<%--
  Created by IntelliJ IDEA.
  User: yhu
  Date: Jan 7, 2010
  Time: 10:55:42 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>


<a href="http://twitter.com/membible">Memorize the Word, The verse to memorize today</a>
<br/>

  <g:each in="${membibles}" status="j" var="mem">
    <br/>

    <p>${mem.replaceAll("membible:","")}</p>
  </g:each>
<br/>
<br/>

<a href="http://twitter.com/gsword">Meditate the Word</a>
<br/>

  <g:each in="${meditates}" status="j" var="med">
    <br/>

    <p>${med.replaceAll("gsword:","")}</p>
  </g:each>

