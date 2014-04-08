<%--
  Created by IntelliJ IDEA.
  User: Yiguang
  Date: Jul 8, 2009
  Time: 9:29:22 PM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<%

  def v = params.get("verse");

  def tt = ""

  if (book) {
    tt += " " + book?.initials
    if (bible) {
      tt += " " + bible?.name
      if (chap ) {
        tt += " " + chap
      //  println " v "+v
       if (v) {
        //  println "v:"+v
         tt += ":" + v
        }
      }
    }
  }
  
   tt+=" in the Bible "
%>
<head><title>${tt} - GSword </title>
  <meta name="description" content="CCIM Chinese Christian GSword Online Bible Studio"/>
     <meta name="keywords" content="${keyword},CCIM,GSword,Jsword,Bible,Chinese,groovy,grails"/>
     <meta name="layout" content="main" />
</head>
<body><h1>${tt}</h1>
%{--<div class="paginateButtons">--}%
%{--<span class="pager-list"><strong class="pager-current">--}%
%{--<c:gpaginate total="${books.size()}" controller="bible" action="read" params="['':params.pth]" />--}%
%{--</strong>--}%
%{--</span></div>--}%
<div style="padding: 1px"><div style="background:#DFDDD1;font-weight: bold; padding:1px">
 <span class="pgray">
<g:if test="${layer==0}">
  <g:render template="includes/genbook"/>
</g:if>

 </span>
   
              </div></div>
${txt}

<a href="${createLink(controller:'bible', action:'search')}">Search | </a>
<a href="${createLink(controller:'gbook', action:'v')}">GSword</a>
</body>
</html>
