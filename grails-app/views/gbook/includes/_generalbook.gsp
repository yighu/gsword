<table id="searchBox" class ='dialog'>
    <tr > <td>
  <g:each in="${books}" status="i" var="book">
       <g:link action="c" id="${book.initials}">${book.name.encodeAsHTML()} |</g:link>
    </g:each>
      </td>
    </tr>   
</table>

      <table>
<tr width="100%">
<td width="100%">

            <g:message code="selectchapter" />:
			<g:select name="genbooksch" id="genbooksch" width="600px"
			  from="${gendropdowntoc}" value="name" optionKey="value" optionValue="value" onchange="searchGen();"/>

</td>
</tr>
    <tr>
    <td width="600px" valign="top">
	<div id="genform">
       ${txt}
	</div>
    </td>
    </tr>
<tr>
<td width="100%">
<hr/>

            <g:message code="selectchapter" />:
			<g:select name="genbooksch2" id="genbooksch2" 
			  from="${gendropdowntoc}" value="name" optionKey="value" optionValue="value" onchange="searchGen2();"/>
<hr/>
</td>
</tr>
<tr>
<td width="100%">
<hr/>
     ${toc}
</td>
</tr>  
</table>
  
