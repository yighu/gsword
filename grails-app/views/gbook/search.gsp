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

  <link rel="stylesheet" type="text/css" href="${createLinkTo(dir:pluginContextPath,file:'css/iBD.css')}" />

  <g:javascript library="prototype"/>

<g:javascript library="application" />
<modalbox:modalIncludes />
<g:javascript>
var selectedbooks=new Array();
function addBooks(selected){
        var len=selectedbooks.length;
        if (len<4){
        var index=selectedbooks.indexOf(selected);
        if (index==-1){
                selectedbooks.push(selected);
        }
        }
}
function removeBooks(selected){
        var index=selectedbooks.indexOf(selected);
        if (index!=-1){
                selectedbooks.splice(index,1);
        }
}
function pick_parallel(){
        addBooks(getParallel());
        locate();
        }
function unpick_parallel(){
        removeBooks(getParallel());
     locate();
        }
function getBooks(){
    if(selectedbooks.length==0){
        return getBook();
    }else{
  return getBook()+","+selectedbooks.toString();
    }
}
function getDictionary()
{
  return $("dictionaries").value;
}
function getCommentary()
{
  return $("commentaries").value;
}
function getParallel()
{
  return $("parallels").value;
}

function searchBible(){
    var bible=$('books')  ;
    var key=$('keyword').value;
    if (key){
    ${remoteFunction(
          controller: 'gbook',
          action: 'searchBible',

          params: '\'bible=\' + escape(bible.value)+\'&key=\'+key',
          onComplete: 'updateForm(e)')};
    }else{
        locate()               ;
    }
}

function locate(){
  var bible=getBooks();
    var reference=$('reference').value;
    ${remoteFunction(
            controller: 'gbook',
            action: 'display',
            params: '\'bible=\' + escape(bible)+\'&key=\'+escape(reference)+\'&start=\'+verseStart+\'&limit=\'+verseLimit',

            onComplete: 'updateForm(e)')};

}
  function setInfo(msg){
  $('info').innerHTML=msg;
  }
  function updateForm(e){
         var result=eval( e.responseJSON  )
      if(result.data){

      var fom=$('liveform');
      fom.innerHTML=result.data;
      }
      if(result.verses){

      var  fom1=$('reference');
           fom1.value= result.verses;
      }
      if( result.total){

        fom1=$('total');
           fom1.value= result.total;
      }
  }
 function openwin(e){
         var result=eval( e.responseJSON ) ;
	var ptf="http://www.ccimweb.org/gsword/"+result.data;
          setInfo("<a href=\""+ptf+"\">Download PowerPoint Here</a>") ;
          popup(ptf, "PowerPoint") ;

  }

function popup(mylink, windowname)
{
if (!window.focus)return true;
var href;
if (typeof(mylink) == 'string')
   href=mylink;
else
   href=mylink.href;
window.open(href, windowname, ',type=fullWindow,fullscreen,scrollbars=yes');
return false;
}

 function updateReference(e){
      var fom=$('reference');
      fom.innerHTML=e.responseText;
  }
var verseStart=0           ;
var verseLimit=10           ;
function prev()
{
        verseStart=verseStart-verseLimit;
        if (verseStart<0){
                verseStart=0;
        }
    return page( verseStart)  ;

}
function nextstep()
{

    var total=$('total').value;
        if (verseStart<0){
            verseStart=0   ;
        }
        verseStart=verseStart+verseLimit;
        if (verseStart>total){
                verseStart=0;
        }

  return   page( verseStart)  ;

}
function page(startv){
var book=getBooks();
  var ref  = $('reference').value;
  if (book && ref)
  {
    getOsis(book, ref, startv,verseLimit);
      return true;
  }
    return false;
}
function getOsis(bible,ref,start,limit){
    ${remoteFunction(
            controller: 'gbook',
            action: 'getOsis',

            params: '\'bible=\' + escape(bible)+\'&reference=\'+escape(ref)+\'&start=\'+start+\'&limit=\'+limit',
            onComplete: 'updateForm(e)')};
}
function getBook()
{
  var bk=$('books').value;
        if(!bk){
        bk='ChiUns';
        setBook(bk);
        }
        return bk;
}
function setBook(data){
    $('books').value=data;
    return false;
}
 function setPassage(data){
 $('reference').value=data;
     locate();
     return false;
 }
 function handlePassage(data){
 $('reference').value=data;
     locate();
     return false;
 }
function updateChapters(data)
{
  var bk=$('chapters');
       var dd=eval(data.responseJSON)    ;
  $('chapters').options[0] = new Option("All Chapters",0);
 var chps=""
    var book=$('bibles').value   ;
for (i = 0; i < dd.nchaps; i++) {
  var newOption = document.createElement("OPTION");
  newOption.text=1+i;
  newOption.value=1+i;
  $('chapters').options[i] = new Option(newOption.text,newOption.value);
  chps=chps+" <a href=\"bible://"+book+(1+i) ;
    chps=chps+" \" onclick='return readChap(&quot;"+book +" "+(1+i) +"&quot;);'>"+(1+i)+"</a> " ;
}
 $('chapters').length = dd.nchaps+1;
 $('chaps').innerHTML=chps;
}

function updateChaptersbybook(bible,data)
{
  var bk=$('chapters');
       var dd=eval(data.responseJSON) ;
  $('chapters').options[0] = new Option("All Chapters",0);
 var chps=""
    var book=bible;
for (i = 0; i < dd.nchaps; i++) {
  var newOption = document.createElement("OPTION");
  newOption.text=1+i;
  newOption.value=1+i;
  $('chapters').options[i] = new Option(newOption.text,newOption.value);
  chps=chps+" <a href=\"bible://"+book+(1+i) ;
    chps=chps+" \" onclick='return readChap(&quot;"+book +" "+(1+i) +"&quot;);'>"+(1+i)+"</a> " ;
}
 $('chapters').length = dd.nchaps+1;
 $('chaps').innerHTML=chps;
}

function setChaps(bible){

${remoteFunction(
           controller:'gbook',
           action:'getChaps',
           params:'\'bible=\' + escape(bible)',
           onComplete:'updateChaptersbybook(bible,e)')};
     updateReference(bible+" 1");
           return false;
}

function readChap(ref){
    $('reference').value=ref;
   // getOsis($('books').value,ref,0,500) ;
    locate();
    return false;
}
function updateReference(data){
 $('reference').value=data    ;
    locate()                   ;
}
function searchDictionary(){
    var dic=$('dictionaries')        ;
    var key=$('keyword')              ;
    ${remoteFunction(
            controller: 'gbook',
            action: 'searchDictionary',

            params: '\'dic=\' + escape(dic.value)+\'&key=\'+key.value',
            onComplete: 'updateDict(e)')};

}
function searchDictionaryp(){
    var dic=$('dictionariesp')        ;
    var key=$('keyp')              ;
    ${remoteFunction(
            controller: 'gbook',
            action: 'searchDictionary',

            params: '\'dic=\' + escape(dic.value)+\'&key=\'+key.value',
            onComplete: 'updateDictp(e)')};

}
function tweet(){
    
   ${remoteFunction(
            controller: 'gbook',
            action: 'ontwitter',
            onComplete: 'twitter(e)')};
  //  Tweek.begin();
}
function updateDict(data){
           var dic=eval(data.responseJSON);

       //$('display_dict').innerHTML=dic.data;
	showBox(dic.data);
	
}
function updateDictp(data){
           var dic=eval(data.responseJSON);

       $('display_dictp').innerHTML=dic.data;
}
function twitter(data){
    var msg=eval(data.responseJSON) ;
   $('display_dict').innerHTML=msg.data     ;
}
   function getPassage(){
   return $('reference').value               ;
   }
function pick_commentary()
{
  var dict=  $('commentaries').value;
  var ref  = getPassage();
  if (dict&& ref)
  {
        addBooks(dict);
locate();
      
  //  return false;
  }
 // return false;
}
function unpick_commentary()
{
  var dict= $('commentaries').value;
  var ref  = getPassage();
  if (dict&& ref)
  {
        removeBooks(dict);
locate();
  //  return false;
  }
  //return false;
}
  function showword(bible){
    ${remoteFunction(
             controller: 'gbook',
             action: 'daily',
            params: '\'bible=\' + escape(bible)',
             onComplete: 'updateForm(e)')};
  setBook(bible);
  }
 function doSearch(){
     var key="";
     var range=$('range').value;
     if (range=='Custom'){
        var custrange=$('customrange').value;
        if (custrange){
          key="["+custrange+"]";
        }
     }else{
       var b=1+range.indexOf('\(');
       var e=range.indexOf('\)');
        if (b>1){
        key="["+range.substring(b,e)+"]";
        }
       var phrase=$('phrase').value;
     if(phrase){
     key+=" \""+phrase+"\"";
     }
     }
       var inwords=$('inwords').value;
     if(inwords){
      key+=" +"+inwords;
     }
       var exwords=$('exwords').value;
       if(exwords){
      key+=" -"+exwords;
     }
       var seems=$('seems').value;
     if(seems){
       key+=" "+seems+"~ ";
     }
       var starts=$('starts').value;
       if(starts){
       key+=" "+starts+"* ";
     }
        
       $('keyword').value=key;
       Modalbox.hide();
       return false;
   }

function sendmail(){
    var name=$('username')                    ;
    var email=$('useremail')                   ;
    var comment=$('usercomment')                ;
      ${remoteFunction(
             controller: 'gbook',
             action: 'sendmail',
             params: '\'name=\' + escape(name.value)+\'&email=\'+escape(email.value)+\'&comment=\'+escape(comment.value)',
             onComplete: 'displaymailsendresponse(e)')};
       Modalbox.hide();
       return false;
   }
function displaymailsendresponse(e){
       var result=e.responseText;

      var fom=$('emailstatus');
      fom.innerHTML=result;
}
function genppt(){
    setInfo("Please wait generating PowerPoint.....")  ;
    var bible=getBook();
    var reference=$('reference').value;
    ${remoteFunction(
             controller: 'gbook',
             action: 'ppt',
 params: '\'version=\' + escape(bible)+\'&key=\'+escape(reference)',

             onComplete: 'openwin(e)')};
}

function showxref(){
    var bible=getBooks();
    var reference=$('reference').value;

    ${remoteFunction(
             controller: 'gbook',
             action: 'flipXRef',
 params: '\'version=\' + escape(bible)+\'&key=\'+escape(reference)+\'&start=\'+verseStart+\'&limit=\'+verseLimit',

             onComplete: 'updateForm(e)')};
}

function showheadings(){
    var bible=getBooks();
    var reference=$('reference').value;

    ${remoteFunction(
             controller: 'gbook',
             action: 'flipHeadings',
 params: '\'version=\' + escape(bible)+\'&key=\'+escape(reference)+\'&start=\'+verseStart+\'&limit=\'+verseLimit',

             onComplete: 'updateForm(e)')};
}

function showstrongs(){
    var bible=getBooks();
    var reference=$('reference').value;

    ${remoteFunction(
             controller: 'gbook',
             action: 'flipStrongs',
 params: '\'version=\' + escape(bible)+\'&key=\'+escape(reference)+\'&start=\'+verseStart+\'&limit=\'+verseLimit',

             onComplete: 'updateForm(e)')};
}

function shownotes(){
    var bible=getBooks();
    var reference=$('reference').value;

    ${remoteFunction(
             controller: 'gbook',
             action: 'flipNotes',
 params: '\'version=\' + escape(bible)+\'&key=\'+escape(reference)+\'&start=\'+verseStart+\'&limit=\'+verseLimit',

             onComplete: 'updateForm(e)')};
}
function donothing(){
}
function showmorph(){
 var bible=getBooks();
    var reference=$('reference').value;

    ${remoteFunction(
             controller: 'gbook',
             action: 'flipMorph',
 params: '\'version=\' + escape(bible)+\'&key=\'+escape(reference)+\'&start=\'+verseStart+\'&limit=\'+verseLimit',
             onComplete: 'updateForm(e)')};
}
function showverseline(){
var bible=getBooks();
    var reference=$('reference').value;
            
    ${remoteFunction(
             controller: 'gbook',
             action: 'flipVline',
 params: '\'version=\' + escape(bible)+\'&key=\'+escape(reference)+\'&start=\'+verseStart+\'&limit=\'+verseLimit',
             onComplete: 'updateForm(e)')};
}
function doProtocol(protocol,lemma){
       ${remoteFunction(
             controller: 'gbook',
             action: 'handleProtocol',
            params: '\'protoc=\' + escape(protocol)+\'&key=\'+lemma',
             onComplete: 'showProtocolData(e)')};
    return false;
}
function showProtocolData(e){
     var result=eval( e.responseJSON  )   ;
     var fom=$('display_dict');
      //fom.innerHTML=result.result;
      showBox(result.result); 
    return false;
}
function showBox(e){
Modalbox.show(e, {title:"Definition",width: 500, height:400,autoFocusing:true});
return false;
}
function changeLocale(){
    
    location.reload("/gsword/gbook/v?lang=\'"+$('lang').value+"\'");
    return false;

}
 function custom(){
  var cust=$('range').value;
     
     if (cust=='Custom'){
     showLayer('xcustomrange');
     }else{
         hideLayer('xcustomrange');
     }
     return false;
 }
   function showLayer(divName) {
        document.getElementById(divName).style.display = "";
        document.getElementById(divName).style.visibility = 'visible';
    }

    function hideLayer(divName) {
        document.getElementById(divName).style.display = "none";
        document.getElementById(divName).style.visibility = 'hidden';
    }
  </g:javascript>


    <title>Advanced Search</title></head>
  <body>
     Search For verses with the following details
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
            <g:select name="range"  onChange="custom();"
                           from="${books}"/>
               
          </td>
      </tr>
     <tr >
         <td>
 <button type="button" id="adsearchbut" onclick="doSearch();">Search</button>         </td>
         <td id="xcustomrange" name="xcustomrange" style="visibility:hidden;display:none;">
         <g:textField id="customrange" name="customrange" />
         </td>
     </tr>
  </table>

  </body>
</html>
