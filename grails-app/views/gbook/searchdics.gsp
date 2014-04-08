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
  //System.out.println "total:"+total   +" totalkeye:"+totalkeye+" totalkeyc:"+totalkeyc +" totalkeycs:"+totalkeycs
  def v = params.get("verse");

  def tt = ""
  def h = ""
  if (keyword) {
    tt += keyword
    h += keyword
    if (bibleversion) {
      if (params?.containsKey("offset")) {
        tt += " - " + params.get("offset")
      }
      tt += " in the Bible " + bibleversion
      h += " in the Bible " + bibleversion
    }

  }
  def ks = txt?.replaceAll("\\<.*?>", "");
%>
<head>
  <gui:resources components="autoComplete" mode="debug"/>
  <title>${tt} - GSword Dictionary ${ks}</title>
  <meta name="description" content="${tt} CCIM Chinese Christian GSword Online Bible Studio Dictionary"/>
  <meta name="keywords" content="${keyword},${tt},${ks}CCIM,word,GSword,Jsword,Bible,Chinese,groovy,grails,dictionary"/>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
  <g:javascript library="prototype"/>
  <g:javascript library="application"/>
  <g:javascript library="lightalert"/>

  <g:javascript>

function searchDictionary(){
    var dic=$('dic')        ;
    var key=$('key')              ;
    ${remoteFunction(
            controller: 'gbook',
            action: 'searchDictionary',

            params: '\'dic=\' + escape(dic.value)+\'&key=\'+key.value',
            onComplete: 'updateDict(e)')};

}
function updateDict(data){
           var dic=eval(data.responseJSON);

       $('display_dict').innerHTML="<b>"+$('key') .value+"</b><br/><br/>"+dic.data;
}
 function handlePassage(refs){
	setPassage(refs);
}
 function setPassage(reference){
     var bible='KJV';

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
  <meta name="layout" content="main"/>

</head>
<body class="yui-skin-sam">   <br/>
<g:form>
  <table>
  <tr>
  <td>
    <g:message code="searchword" />
  </td>

 <td width="200">
<!--
  <gui:autoComplete
          id="key"
          controller="gbook"
          action="suggestedkeys"
	 idField="id"
          resultName="keys"
         forceSelection="true"
          dependsOn="dic"
     useShadow="false"
  />
-->
<g:textField name="key"  />
 </td>
 <td>
    <g:message code="selectdic" />
   </td>
    <td>

  <g:select name="dic" id="dic" class='dictionaries' noSelection="['easton':'Eastons Bible Dictionary']"
          from="${dictionaries}" value="name" optionKey="initials" optionValue="name" onchange="searchDictionary(); "/>
    </td>
    <td>
      <button type="button" id="searchButtondic" onclick="searchDictionary();"><g:message code="lookupdict" /></button>
      
    </td>
   </tr>
  </table>
  </g:form>
 <br/>
  <div id="display_dict">
   <b> ${keyword}</b> <br/><br/>${keyvalue}
    </div>
<br/>
      <g:if test="${bookkey}">
        <div style="padding: 10px"><div style="background:#DFDDD1;font-weight: bold; padding:10px">
                  <span class="pgray">
<div class="paginateButtons">
  ${dic} 
<span class="pager-list"><strong class="pager-current">
<g:paginate total="${bookkey.getCardinality()}" controller="gbook" action="searchdics" params='["":"${dic?.trim()}"]' />
</strong>
</span></div>

                  </span>
              </div></div>
      </g:if>

      <br/>
<g:render template="includes/dictionaries"/>

<a href="/gsword"><g:message code="home" /> |</a>

<a href="${createLink(controller: 'bible', action: 'read')}"><g:message code="searchbible"/></a>

<a href="${createLink(controller: 'gbook', action: 'v')}">|GSword</a>
</body>
</html>
