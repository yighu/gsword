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
      tt += " " + bible?.shortname
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
  def ks=txt?.replaceAll("\\<.*?>","");

%>
<head><title>${tt} - GSword What does Bible say? ${ks} </title>
  <meta name="description" content="${tt} ${ks} what does Bible say CCIM Chinese Christian GSword Online Bible Studio "/>
     <meta name="keywords" content="${keyword},${tt},${ks}, what does Bible say CCIM,word,GSword,Jsword,Bible,Chinese,groovy,grails"/>
     <meta name="layout" content="main" />
  <g:javascript library="prototype"/>
<g:javascript library="application" />
<g:javascript library="lightalert" />
<g:javascript>
 function handlePassage(reference){
 	setPassage(reference);
	}
 function setPassage(reference){
     var bible='kjv';

    ${remoteFunction(
          controller: 'gbook',
          action: 'display4g',
          params: '\'bible=\' + escape(bible)+\'&key=\'+escape(reference)',

          onComplete: 'updateForm(e)')};

         return false;
}
function updateForm(e){
         var result=eval( e.responseJSON  )
      if(result.data){
alert(result.data);
      }
}
</g:javascript>
</head>
<body><!--h1>${tt}</h1-->
%{--<div class="paginateButtons">--}%
%{--<span class="pager-list"><strong class="pager-current">--}%
%{--<c:gpaginate total="${books.size()}" controller="bible" action="read" params="['':params.pth]" />--}%
%{--</strong>--}%
%{--</span></div>--}%
<div style="padding: 1px"><div style="background:#DFDDD1;font-weight: bold; padding:1px">
 <span class="pgray">
  <g:render template="includes/vcmnt"/>
  <g:render template="includes/cmntbibles"/>
 </span>
   
              </div></div>
${txt}
<a href="/gsword" ><g:message code="home" /> |</a>

<a href="${createLink(controller:'bible', action:'search')}">Search | </a>
<a href="${createLink(controller:'gbook', action:'v')}">GSword</a>

</body>
</html>
