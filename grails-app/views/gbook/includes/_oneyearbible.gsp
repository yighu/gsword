<g:javascript library="application" />
<center>

<g:message code="subscribe"/>:    <a href="http://bible.ccim.org/email/bibleemaillist_zh.htm">Chinese</a> <a href="http://bible.ccim.org/email/bibleemaillist.htm">English</a>
<br/>

            <g:message code="oneyearbible" />:
			<g:select name="dailyword" class ="books" noSelection="['':'']"
                from="${books}" value="name" optionKey="initials" optionValue="name" onchange="showword(this.value); " />
</center>

<br/>
      <div id="liveform">
      ${results}
      </div>
