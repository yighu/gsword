<!-- fieldset legend="search area" -->
<g:javascript library="application" />
  <modalbox:modalIncludes />
 
  <table id="searchBox" class ='dialog'>
	<tr class ='title'>
		<td colspan ='12' ><g:message code="studio" />

		</td>
        <td>
          <a href="/gsword/help/help.xhtml" target="_blank"><g:message code="help" /></a>
        </td>
	</tr>
    <tr >

		<td class ="labelright" >
            <g:message code="searchbible" />:
		</td>
		<td title="Select a Book to read">
			<g:select name="books" id="books" class ="books"
			  from="${books}" value="name" optionKey="initials" optionValue="name" onchange="expGenerics();"/>
		</td>
		<td>
			<button type="button" id="searchButton" title="Click to Search the Key word in selected Bible" onclick="expGenerics();"><g:message code="lookup" /></button>
		</td>

		<td class ='labelright'>
			<g:message code="choosebook" />: 
		</td>
		<td>
			<g:select name="bibles" id="bibles" class ='bibles' noSelection="['':'']"
              from="${bibles}"  
            optionValue="shortname"
                optionKey="key"
           
              onchange="${remoteFunction(
           controller:'gbook',
           action:'getChaps',
           params:'\'bible=\' + escape(this.value)',
           onComplete:'updateChapters(e)')}"/>
		</td>
		<td class ="labelright" >
            <g:message code="oneyearbible" />:
		</td>
		<td title="select a bible for daily reading">
			<g:select name="dailyword" class ="dailyword" noSelection="['':'']"
                from="${books}" value="name" optionKey="initials" optionValue="name" onchange="showword(this.value); " />

		</td>

    </tr>
    
</table>
<table>
	<tr>
		<td   colspan =4 height =10px></td>
	</tr>
    <tr>
		<td  class ="labelright">
			<g:message code="total" />:&nbsp;<input type="text" id="total" name="total" value="${total}" size=5 readonly/>
		</td>
		<td>
			&nbsp;
			<button type="button" id="prev" onclick="prev();" title="Click for previous 10 verses">< <g:message code="default.paginate.prev" /></button>&nbsp;
			<button type="button" id="next" onclick="nextstep();" title="Click for next 10 verses"><g:message code="default.paginate.next" /> ></button>
		</td>
        <td>
            <button type="button" id="showstrongs" onclick="showstrongs();" title="Click to on/off Strong number. Best used with KJV Bible"><g:message code="strongs" /></button>&nbsp;

        </td>
         %{--<td>--}%
             %{--<button type="button" id="showmorph" onclick="showmorph();"><g:message code="morphology" /></button>&nbsp;--}%

         %{--</td>--}%
          <td>
              <button type="button" id="showverseline" onclick="showverseline();" title="Click to swith 1 or multiple verses a line"><g:message code="verseperline" /></button>&nbsp;

          </td>

         <td title="Click to generate PowerPoint file">
           <button type="button" id="ppt" onclick="genppt();">PowerPoint</button>&nbsp;
           %{--<button type="button" id="excl" onclick="genexcl();">Excel</button>&nbsp;--}%

             <g:link action="language_change" params="[lang:'zh_CN']" title="click to use Chinese"><g:message code="locale.language.zh" /></g:link>
             <g:link action="language_change" params="[lang:'en_US']" title="click to use English"><g:message code="locale.language.en" /></g:link>
           <a href="http://www.ccim.org/n103" target="_blank" title="Support information"><g:message code="donation" /></a>
           <modalbox:createLink controller="gbook" action="contactus" title='Comments' width="400" linkname='Comments' />

          </td>
      <td><a href="http://www.facebook.com/group.php?gid=1714831533"><img src="/gsword/images/ipcn-fb.png" width="55" height="25" alt="facebook" title="facebook" border="0" /></a></td>
      <td><a href="http://www.ccimweb.org/gsword/gbook/feed"><img border="0" src="/gsword/images/icon-rss.png" width="55" height="25" alt="rss" title="rss" /></a></td>
      <td><a href="http://www.twitter.com/gsword"><img border="0" src="/gsword/images/icon-twitter.png" width="55" height="25" alt="twitter" title="twitter" /></a></td>
      

    </tr>


</table>

<table>
  <tr> 
  <td width="10" valign="top" title="Select a book to read">
    
      <g:message code="books" />
     <g:if test="${bibles}">
            <g:each in="${bibles}" status ="i" var="b">
            <a href="bible://${b.shortname}"  id="b${b}" onClick='return setChaps(&quot;${b.key}&quot;);'>
             ${b.shortname}
           </a>
            </g:each>

             </g:if>
  </td>
  <td valign="top" title="Select a chapter to read">
      <table>
   <tr><td id="xchaps" name="xch" valign="top">
      <g:message code="chapters" />   <div id="chaps" name="chaps" />
      </td>
   
  <tr>
    <td id="display" width="80%" valign="top">

      <div id="liveform">

       ${txt}
      </div>
    </td>


  </td>

  </tr>
</table>
    </td>
</table>
