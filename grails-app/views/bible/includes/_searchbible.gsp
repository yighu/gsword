<g:form name="searchform" method="POST" url='[controller: "bible", action: "seek"]'>
   <g:hiddenField name="version" value="${bibleversion}" /> 
   <g:textField name="key"  />  
 <g:actionSubmit class="search" value="Search Bible" action="seek"/>
</g:form>
