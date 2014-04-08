import java.text.SimpleDateFormat
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.sql.*;
import groovy.xml.MarkupBuilder
import javax.servlet.http.HttpSession
import javax.xml.transform.TransformerException
import org.crosswire.bibledesktop.desktop.XSLTProperty
import org.crosswire.common.util.NetUtil
import org.crosswire.common.util.ResourceUtil
import org.crosswire.common.xml.SAXEventProvider
import org.crosswire.common.xml.TransformingSAXEventProvider
import org.crosswire.common.xml.XMLUtil
import org.crosswire.jsword.bridge.BookInstaller
import org.crosswire.jsword.passage.Key
import org.crosswire.jsword.passage.NoSuchKeyException
import org.crosswire.jsword.passage.Passage
import org.crosswire.jsword.versification.BibleInfo
import org.crosswire.jsword.versification.BibleNames
import org.xml.sax.SAXException
import org.crosswire.jsword.book.*
import grails.validation.Validateable

  //static navigation = true
  binding['BIBLE_PROTOCOL'] = "bible";                                     //$NON-NLS-1$
  binding['DICTIONARY_PROTOCOL'] = "dict";                                      //$NON-NLS-1$
  binding['GREEK_DEF_PROTOCOL'] = "gdef";                                      //$NON-NLS-1$
  binding['HEBREW_DEF_PROTOCOL'] = "hdef";                                      //$NON-NLS-1$
  binding['GREEK_MORPH_PROTOCOL'] = "gmorph";                                    //$NON-NLS-1$
  binding['HEBREW_MORPH_PROTOCOL'] = "hmorph";                                    //$NON-NLS-1$
  binding['COMMENTARY_PROTOCOL'] = "comment";                                   //$NON-NLS-1$
  binding['STRONGS_NUMBERS'] = "Strongs"; //$NON-NLS-1$
  binding['NOTES'] = "Notes"; //$NON-NLS-1$
  binding['BCVNUM'] = "BCVNum"; //$NON-NLS-1$
  binding['VNUM'] = "VNum"; //$NON-NLS-1$
   def languageService
  //def scaffold = Gbook
  def index = {
    redirect(action: read)
  }
  def beforeInterceptor = {
    def key = "org.springframework.web.servlet.DispatcherServlet.LOCALE_RESOLVER"
    def localeResolver = request.getAttribute(key)
    Locale locale = localeResolver.resolveLocale(request)
  //  println " do update " +locale
    if (locale != null) {
      localeResolver.setLocale(request, response, locale)
      Locale.setDefault(locale);
      String langename = locale.getDisplayLanguage()
  //    println " language=" + langename
      if (langename.equals("English")) {
        if (session.englishbibles == null) {
          updateBibles(Locale.US)
          session.englishbibles = bibles
          session.englishbiblekeymap = biblekeymap
        } else {
          bibles = session.englishbibles
          biblekeymap = session.englishbiblekeymap

        }
      } else {

        if (session.chinesebibles == null) {

          updateBibles(Locale.CHINA)

          session.chinesebibles = bibles
          session.chinesebiblekeymap = biblekeymap

        } else {
          bibles = session.chinesebibles
          biblekeymap = session.chinesebiblekeymap
        }
      }
      session.currentlaunage = langename

    }

  }

  def language_change = {
    /* def lang=params.lang
    if (lang?.equals("en_US")){
      updateBibles(Locale.US)

    } else{
    updateBibles(Locale.CHINA)
    }*/
    redirect(action: v, params: params)

  }
  
  private findVersion(List books, String version) {
    def rst
    books.each {book ->
	println book.initials
      if (book.initials.equalsIgnoreCase(version)) {
        rst = book
      }
    }
     println " version rst:"+rst
    rst
  }

  private findBible(List bibles, String name) {
    def rst
//println "find bible:"+name
    bibles.each {bible ->
	//println "key:"+bible.key	
      if (bible.key.equals(name)) {
        rst = bible
      }
    }

    rst
  }
  def readgen={
    List books = Books.installed().getBooks(BookFilters.getGeneralBooks());
       session.state_vline=true
     
       def layer=0
         if (params.containsKey("book")) {
           layer=1
           //$book?/$chapter?/$verse?
           def booktxt = params.get("book")
           Book bok = jswordService.getBook(booktxt)

           def chapters = 5;//findChapters(booktxt)
           if (params.containsKey("chapter")) {
             layer=2
             //$version?/$book?/$chapter?/$verse?
             def chap = params.get("chapter")
             def verses = 10;//findVerses(booktxt, Integer.parseInt(chap))
            // println "verses:" + verses
             // def verses = 50;//find # of verses in this chapter
             if (params.containsKey("verse")) {
               layer=3
               //$version?/$book?/$chapter?/$verse?
              // println " still verses:" + verses

               def ver = params.get("verse")
               def ref = bok.key + " " + chap + ":" + ver

               def searchresult = searchByRef(versionobj.initials, ref)// "dis this verse " + ver
               render(view: "readgen", model: [books: books, book: versionobj,layer:layer, bibles: bibles, bible: bok, chapters: chapters, chap: chap, verses: verses, txt: searchresult.get("data")]) //for one chapter

             } else {
               def ref = bok.key + " " + chap
               def searchresult = searchByRef(versionobj.initials, ref)// "dis this verse " + ver

               render(view: "readgen", model: [books: books, book: versionobj, layer:layer,bibles: bibles, bible: bok, chapters: chapters, chap: chap, verses: verses, txt: searchresult.get("data")]) //for one chapter

             }

           } else {
             def ref =  "1"
             def searchresult = searchByRef(bok.getName(), ref)// "dis this verse " + ver
            // println  searchresult
             render(view: "readgen", model: [books: books, book: bok, layer:layer,bibles: bibles, bible: bok, chapters: chapters, txt: searchresult.get("data")]) //for one chapter

           }
         } else {
           def ref = "1"
           def searchresult = searchByRef("Institutes", ref)// "dis this verse " + ver
           render(view: "readgen", model: [books: books, book: "Institutes", layer:layer,bibles: bibles, txt: searchresult.get("data")])   //  one version
         }


  }
  def read = {bibles,params->
    List books = Books.installed().getBooks(BookFilters.getBibles());
	println books.size()
	books.each{
	println it.toString()
	}
    def layer=0
    if (params.containsKey("version") || params.containsKey("id")) {
      layer=1
      def versiontxt = params.get("version")
      if (!versiontxt){
          versiontxt = params.get("id")
      }
      if (!versiontxt){
         versiontxt="kjv"
      }
      if (versiontxt?.equals("read")){
         versiontxt="kjv"
      }
      def versionobj = findVersion(books, versiontxt) //select the book of the version
      if (!versionobj){
        versionobj = findVersion(books, "kjv")
      }
	println "found version:"+versionobj
      if (params.containsKey("book")) {
        layer=2
        def booktxt = params.get("book")
        def bok = findBible(bibles, booktxt) //search the book object of this booktxt
		if (!bok){
			bok=new Expando();
			bok.key="KJV"
		}
        def chapters = findChapters(booktxt)

        if (params.containsKey("chapter")) {
          layer=3
          def chap = params.get("chapter")
          def verses = findVerses(booktxt, Integer.parseInt(chap))
          if (params.containsKey("verse")) {
            layer=4
            def ver = params.get("verse")
            def ref = bok.key + " " + chap + ":" + ver
            def searchresult = searchByRef(versionobj?.initials ?: "ChiUns", ref)// "dis this verse " + ver
            return searchresult.get("data")

          } else {
            def ref = bok.key + " " + chap
            def searchresult = searchByRef(versionobj.initials, ref)// "dis this verse " + ver

            return searchresult.get("data")

          }

        } else {
          def ref = bok?.key +" 1"
	println "versionobj:"+versionobj
          def searchresult = searchByRef(versionobj.initials, ref)// "dis this verse " + ver
            return searchresult.get("data")

        }
      } else {
        def ref = " John 1"
        def searchresult = searchByRef(versionobj?.initials, ref)// "dis this verse " + ver
            return searchresult.get("data")
      }
    } else {
      def ref = "Gen John 1"
      def searchresult = searchByRef("kjv", ref)// "dis this verse " + ver
            return searchresult.get("data")
    }
  }
   def cmnt = {
    session.state_vline=true
    List books = Books.installed().getBooks(BookFilters.getCommentaries());
    def bibles = getBibles(session)

    def layer=0
    if (params.containsKey("version") || params.containsKey("id")) {
      layer=1
      def versiontxt = params.get("version")
      if (!versiontxt){
          versiontxt = params.get("id")
      }
      if (!versiontxt){
         versiontxt="MHCC"
      }
      if (versiontxt?.equals("cmnt")){
         versiontxt="MHCC"
      }
      def versionobj = findVersion(books, versiontxt) //select the book of the version
      if (!versionobj){
        versionobj = findVersion(books, "MHCC")
      }
      if (params.containsKey("book")) {
        layer=2
        def booktxt = params.get("book")
        def bok = findBible(bibles, booktxt) //search the book object of this booktxt
		if (!bok){
			bok=new Expando();
			bok.key="KJV"
		}
        def chapters = findChapters(booktxt)

        if (params.containsKey("chapter")) {
          layer=3
          def chap = params.get("chapter")
          def verses = findVerses(booktxt, Integer.parseInt(chap))
          if (params.containsKey("verse")) {
            layer=4
            def ver = params.get("verse")
            def ref = bok?.key + " " + chap + ":" + ver

            def searchresult = searchByRef(B4C+versionobj?.initials ?: "MHCC", ref)// "dis this verse " + ver
            render(view: "readcmnt", model: [books: books, book: versionobj,layer:layer, bibles: bibles, bible: bok, chapters: chapters, chap: chap, verses: verses, txt: searchresult.get("data")]) //for one chapter

          } else {
            def ref = bok.key + " " + chap
            def searchresult = searchByRef(B4C+versionobj.initials, ref)// "dis this verse " + ver

            render(view: "readcmnt", model: [books: books, book: versionobj, layer:layer,bibles: bibles, bible: bok, chapters: chapters, chap: chap, verses: verses, txt: searchresult.get("data")]) //for one chapter

          }

        } else {
          def ref = bok.key +" 1"
          def searchresult = searchByRef(B4C+versionobj.initials, ref)// "dis this verse " + ver
          render(view: "readcmnt", model: [books: books, book: versionobj, layer:layer,bibles: bibles, bible: bok, chapters: chapters, txt: searchresult.get("data")]) //for one chapter

        }
      } else {
        def ref = " John 1"
        def searchresult = searchByRef(B4C+versionobj?.initials, ref)// "dis this verse " + ver
        render(view: "readcmnt", model: [books: books, book: versionobj, layer:layer,bibles: bibles, txt: searchresult.get("data")])   //  one version
      }
    } else {
      def ref = "Gen John 1"
      def searchresult = searchByRef(B4C+"MHCC", ref)// "dis this verse " + ver
      render(view: "readcmnt", model: [books: books, layer:layer,txt: searchresult.get("data")])            //all versions
    }
  }
   def dics= {
    List books = Books.installed().getBooks(BookFilters.getDictionaries());
    def data= jswordService.getOSISString(params.dic, params.key, 0, 10)
    render(view: "readdics", model: [books: books,txt: data])
  }
  def B4C="ChiUns,KJV,"

  def retrivekeybyid(String version, int offset){
	def key
         if (version.equalsIgnoreCase("kjv")){
	  if (offset>bibleindexService.thekjvKeys()?.size())offset=bibleindexService.thekjvKeys()?.size()-1
         key=bibleindexService.thekjvKeys()?.get(offset)
         }else if (version.equalsIgnoreCase("chiun")){
           if (offset>3124)offset=3124
           key=bibleindexService.thechiunKeys()?.get(offset)
         } else{
            if (offset>3102)offset=3102
           key=bibleindexService.thechiunsKeys()?.get(offset)
         }
	key
	}
  def retrivekeyindex(String version, String key){
	def keyindex
         if (version.equalsIgnoreCase("kjv")){
	  keyindex=bibleindexService.findindex(bibleindexService.thekjvKeys(),key)
         }else if (version.equalsIgnoreCase("chiun")){
	  keyindex=bibleindexService.findindex(bibleindexService.thechiunKeys(),key)
         }else{
	  keyindex=bibleindexService.findindex(bibleindexService.thechiunsKeys(),key)
         }
	keyindex
	}
  def search = {
    /* params.each{
      println "parms:"+it

    }*/
    def offset=0
    if (params.containsKey("offset")){
      try{
      offset=Integer.parseInt(params.get("offset"))
      }catch (Exception e){}
    }

     def vk=params.get("vk")?.decodeHTML()
    def version="kjv"
    def key="God"
    def keyid=0
     if (vk){
       if (vk.contains("-")){
         int i=vk.indexOf("-")
         version=vk.substring(0,i)
         key=vk.substring(i+1)
	keyid=Integer.parseInt(vk.substring(i+1))
  	key=retrivekeybyid(version, keyid)
       }else{
         version=vk
         if (version.equalsIgnoreCase("kjv")){
	  if (offset>bibleindexService.thekjvKeys()?.size())offset=bibleindexService.thekjvKeys()?.size()-1
         key=bibleindexService.thekjvKeys()?.get(offset)
         }else if (version.equalsIgnoreCase("chiun")){
           if (offset>3124)offset=3124
           key=bibleindexService.thechiunKeys()?.get(offset)
         } else{
            if (offset>3102)offset=3102
           key=bibleindexService.thechiunsKeys()?.get(offset)

         }
  	key=retrivekeybyid(version, offset)
	keyid=offset
       }
     }
    def searchresult=searchByKey(version, key,offset)
    def tt=searchresult.get("total")
	def searchkk=version
	if(keyid>=0)searchkk=version+"-"+keyid
    render(view: "search", model: [bibleversion: version, keyword:key, totalkeycs:bibleindexService.chiunskeysize(),totalkeye:bibleindexService.kjvkeysize(),totalkeyc:bibleindexService.chiunkeysize(),total:tt,txt: searchresult.get("data"),searchkk:searchkk]) //for one chapter

  }
   def seek = {
//     params.each{
//      println "parms:"+it
//
//    }
    def offset=0

     Book book = BookInstaller.getInstalledBook("ZhEnglish");
    Key ke = book.getGlobalKeyList()
         Book bookc = BookInstaller.getInstalledBook("ZhHanzi");
    Key kc = bookc.getGlobalKeyList()
     
     def key=params.get("key")
    def version=params.get("version")?:"KJV"
    version=languageService.therightbible(version,key)
     println "ver:"+version+" key:"+key+" offset:"+offset
    def searchresult=searchByKey(version, key,offset)
     //  Book book = BookInstaller.getInstalledBook("ZhEnglish");

	//println "search result:"+searchresult.get("data")
    def keyindex=retrivekeyindex(version, key)
	def searchkk=version+"-"+keyindex
    render(view: "search", model: [bibleversion: version, keyword:key, totalkeye:ke.cardinality?:10000,totalkeyc:kc.cardinality?:3000,total:searchresult.get("total")?:10,txt: searchresult.get("data"),searchkk:searchkk]) //for one chapter

  }
  def v = {

    //http://www.ccim.org/cgi-user/bible/ob?version=hgb&book=mat&chapter=16&verse=1
    //http://www.ccim.org/cgi-user/bible/v/version=hgb/book=mat/chapter=16/verse=1
    def version = params.version
    if (!version) {
      version = "ChiUns"
    }

    def key
    if (!(params.book || params.chapter || params.verse)) {
      def scheules = getSchedule()
      def td = today()
      key = scheules.get(td)
    } else {
      key = params.book
      if (!params.book) {
        key = "gen"
      }
      def chapter = params.chapter
      if (chapter) {
        key += " " + chapter
      }
      def verse = params.verse

      if (verse) {
        key += ":" + verse
      }
    }
    def start = 0;
    if (params.start) {
      start = Integer.parseInt(params.start) - 1
      if (start < 0) {
        start = 0;
      }
    }
    //controller: 'gbook', action: 'display', params: '\'bible=\' + escape(bible)+\'&key=\'+escape(reference)+\'&start=\'+verseStart+\'&limit=\'+verseLimit',
    //  println "version:" + version
    // println "key:" + key
    //println "start:" + start

    def result = readStyledText(version, key, start, 200)
    def total = jswordService.getCardinality(version, key)

    List books = Books.installed().getBooks(BookFilters.getBibles());
    List dictionaries = Books.installed().getBooks(BookFilters.getDictionaries());
    List commentaries = Books.installed().getBooks(BookFilters.getCommentaries());
    List devotions = Books.installed().getBooks(BookFilters.getDailyDevotionals());
    def bibles = getBibles(session)
    def bible = "KJV"
    def chapters = getChapters(bible);
    def mainbooks = new ArrayList()
    mainbooks.add("ChiUns")
    mainbooks.add("ChiUn")
    mainbooks.add("ChiNCVs")
    mainbooks.add("ChiNCVt")
    mainbooks.add("KJV")
    // mainbooks.add("ESV")
    /*   println "books:"
    books.each {
        println it.getName()
    }*/
    //println "bibles:"
//    books.each{
//      println it.name
//      println it.localename()
//    }
//    println "Dictionaries:"
//     dictionaries.each{
//      println it.name
//    }
//    println "Commentary:"
//     commentaries.each{
//      println it.name
//    }
//    println "devotion:"
//      devotions.each{
//      println it.name
//    }
    render(view: 'searchresults', model: [results: result, ref: key, version: version, total: total, mainbooks: mainbooks, books: books, dictionaries: dictionaries, commentaries: commentaries, bibles: bibles, chapters: chapters, devotions: devotions])


  }
  def listkey = {
   // println "dci=" + params

  //  Book book = BookInstaller.getInstalledBook("ZhEnglish");
    Book book = BookInstaller.getInstalledBook("ZhHanzi");

    Key key = book.getGlobalKeyList()


    key.each {k ->
      println k
    }
//Create XML response
//    println "key;" + key
//    Iterator iter = key.iterator();
//    while (iter.hasNext()) {
//      def k = iter.next()
//      println k
//
//    }
    
  }






  def getPlainText(String bookInitials, String reference, int maxKeyCount, HttpSession session) throws BookException, NoSuchKeyException {
    if (bookInitials == null || reference == null) {
      return null;
    }

    Book book = jswordService.getBook(bookInitials);

    Key key = null;
    List result = new ArrayList()
    if (BookCategory.BIBLE.equals(book.getBookCategory())) {
      key = book.getKey(reference);
      ((Passage) key).trimVerses(maxKeyCount);
      int nv = ((Passage) key).countVerses()
      for (int i = 0; i < nv; i++) {
        def vs = ((Passage) key).getVerseAt(i)
        BookData data = new BookData(book, vs);
        def gv = new Expando()
        gv.book = vs.getBook()
        gv.chapter = vs.getChapter()
        gv.verse = vs.getVerse()
        //      gv.text=(OSISUtil.getCanonicalText(data.getOsisFragment()))?.trim().replaceAll(" ", "")
        //println "Book language="+ book.getLanguage().getName() +" init=" + bookInitials
        // println "OSIS Fragment:"+ data.getOsisFragment()

        if (book.getLanguage().getName().equals("Chinese")) {
          gv.text = (OSISUtil.getCanonicalText(data.getOsisFragment()))?.trim()?.replaceAll(" ", "")?.replaceAll("\t", "").replaceAll("\r", "").replaceAll("\n", "")

        } else {
          try {
            gv.text = OSISUtil.getCanonicalText(data.getOsisFragment())
          } catch (Exception e) {
            //e.printStackTrace()
          }
        }

        gv.name = vs.getName()
        gv.cbook = retrivebookfromreference(vs.getName(), bookInitials, book.getLanguage()?.getName(), session)

        result.add(gv)
      }
    }



    return result
  }


  def fetchbook(String refernce) {
    def digit = 0
    for (int counter = 0; counter < refernce.length(); counter++) {
      if (Character.isDigit(refernce.charAt(counter))) {
        digit = counter
        break
      }

    }
    refernce.substring(0, digit).trim()
  }

  def retrivebookfromreference(String reference, String bookInitials, String language, HttpSession session) {
    def x = fetchbook(reference)
    //   println " language for refer:"+language +" bookInitials="+bookInitials
    String book = x
    if (x?.length() > 1) {
      if (language?.equalsIgnoreCase("Chinese")) {
        if (biblekeymap.containsKey(x)) {
          book = session.chinesebiblekeymap.get(x)
        }
      } else {
        book = x
      }
      //   println "old book x="+x +" book="+book
      return book
    } else {
      if (language?.equalsIgnoreCase("Chinese")) {

        if (biblekeymap.containsKey(reference)) {
          book = session.chinesebiblekeymap.get(reference)
        }
      } else {
        book = reference
      }
      //  println "old book ref="+x +" book="+book
      return book
    }
  }

  public SAXEventProvider getOSIS(String bookInitials, String reference, int maxKeyCount) throws BookException, NoSuchKeyException {
    if (bookInitials == null || reference == null) {
      return null;
    }

    Book book = getBook(bookInitials);

    Key key = null;
    if (BookCategory.BIBLE.equals(book.getBookCategory())) {
      key = book.getKey(reference);
      ((Passage) key).trimVerses(maxKeyCount);
    }
    else {
      key = book.createEmptyKeyList();

      Iterator iter = book.getKey(reference).iterator();
      int count = 0;
      while (iter.hasNext()) {
        if (++count >= maxKeyCount) {
          break;
        }
        key.addAll((Key) iter.next());
      }
    }

    BookData data = new BookData(book, key);

    return data.getSAXEventProvider();
  }


  def nbibles = {
    //org.crosswire.jsword.versification.
    //  BibleNames bn = new BibleNames(Locale.CHINA)
    BibleNames bn = new BibleNames()

    /* for (int i = 1; i <= 66; i++) {
        println bn.getShortName(i) + "/" + bn.getLongName(i);
    }*/
  }


  def searchByRef(String version, String ref) {
    def text = readStyledText(version, ref, 0, 300)
    def total =0
	try{
	total= jswordService.getCardinality(version, ref)
	}catch (Exception e){}
    Map mp = new HashMap();
    mp.put("data", text)
    mp.put("verses", ref)
    mp.put("total", total)
    mp
  }

  def searchByKey(String version, String key) {

    def ref = jswordService.search(version, key)
    def text = readStyledText(version, ref, 0, 10)
    def total = jswordService.getCardinality(version, ref)
    Map mp = new HashMap();
    mp.put("data", text)
    mp.put("verses", ref)
    mp.put("total", total)
    mp
  }
  def searchByKey(String version, String key,int offset) {
	println "in search by key:"+version +" key:"+key+" ofset:"+offset
     def ref = jswordService.search(version, key)
	println "reference from jswordService search:"+ref
     def text = readStyledText(version, ref, offset, 10)
	//println "text:"+text
     def total = jswordService.getCardinality(version, ref)
     Map mp = new HashMap();
     mp.put("data", text)
     mp.put("verses", ref)
     mp.put("total", total?:10)
     mp
   }
  def bibleindexService

  def findChapters(String bible) {
    int bn = BibleInfo.getBookNumber(bible);
    if (bn<1)bn=1
    BibleInfo.chaptersInBook(bn);

  }

  def findVerses(String bible, int chapter) {
    int bn = BibleInfo.getBookNumber(bible);
    //BibleInfo.chaptersInBook(bn);
	if (chapter<1)chapter=1
	if(bn<1)bn=1
    BibleInfo.versesInChapter(bn, chapter)
  }

  def adsearch = {
    def books = org.crosswire.bibledesktop.book.Msg.PRESETS.toString().split("\\|")
    render(view: 'adsearch', model: [books: books])

  }
  def dailyschedule = new HashMap()
  def scheduletxt = ResourceUtil.getResource("daily.txt");

  def getSchedule() {
    if (dailyschedule.isEmpty()) {
      scheduletxt.eachLine {line ->
        // println line
        if (line.trim().length() > 1) {
          def key = line.substring(0, 4)
          def value = line.substring(5).trim().replaceAll(' ', ",")

          dailyschedule.put(key, value)
        }
      }
    }

    dailyschedule
  }

  String today() {
    def dt = new Date();
    java.text.DateFormat dateFormat = new SimpleDateFormat("MMdd")
    dateFormat.format(dt)
  }

  def dailytxt(String book) {
    def scheules = getSchedule()
    def td = today()
    readStyledText(book, scheules.get(td), 0, 500)
  }

  def dailytxt() {
    def scheules = getSchedule()
    def td = today()
    readStyledText(params.bible ?: "KJV", scheules.get(td), 0, 500)
  }

  def daily = {
    def text = dailytxt();
    def mp = new HashMap()
    mp.put("data", text)
    //render mp as JSON
  }

  binding['bibles'] = new ArrayList()
  binding['biblekeymap'] = new HashMap()

  def updateBibles(Locale loc) {
    bibles.clear()
    biblekeymap.clear()
   // println "update bible for locale " + loc
    if (bibles.isEmpty()) {

      int nbook = BibleInfo.booksInBible();

      BibleNames bn = new BibleNames(loc)

      try {
        for (int i = 1; i <= nbook; i++)

        {

          def gb = new Expando()
          gb.key = BibleInfo.getBookName(i).getShortName()
          gb.shortname = bn.getShortName(i)
          gb.cname = bn.getName(i)
          gb.longname = BibleInfo.getBookName(i).getLongName()
          biblekeymap.put(gb.longname, gb.cname)
          biblekeymap.put(gb.shortname, gb.cname)
          biblekeymap.put(gb.key, gb.cname)
          bibles.add(gb)
        }

       // println " local "+loc +" bibe size:"+bibles.size()

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
   // println " bibes: "+bibles.size()
    return bibles
  }


  //  private static final     URL xslurl = ResourceUtil.getResource("xsl/cswing/simple.xsl");

  public String readStyledText(String bookInitials, String reference, int start, int maxKeyCount) throws NoSuchKeyException, BookException, TransformerException, SAXException {
    def mainbook = "John";
//	println "bookInitials:"+bookInitials
   try{
    mainbook = bookInitials.split(',')[0];
   }catch (Exception e){
     println "book ini"+bookInitials
   }
    if(!bookInitials)bookInitials="John"
    //println "main book:"+mainbook
    Book book = jswordService.getBook(mainbook);
    SAXEventProvider osissep = jswordService.getOSISProvider(bookInitials, reference, start, maxKeyCount);
    if (osissep == null) {
      return ""; //$NON-NLS-1$
    }

    //  println "use new style....." +xslurl.getPath()
    TransformingSAXEventProvider htmlsep = new TransformingSAXEventProvider(NetUtil.toURI(xslurl), osissep); //Customize xslt
    //  doStrongs(false)

      def state_strongs = "false"
      htmlsep.setParameter(STRONGS_NUMBERS, state_strongs)
      htmlsep.setParameter(NOTES, "false")
      htmlsep.setParameter(BCVNUM, "true")
 //     htmlsep.setParameter(VNUM, "true")

      def state_morph = "false"
    htmlsep.setParameter("Morph", state_morph)

      def state_vline = "false"
    htmlsep.setParameter("VLine", state_vline)

    //Converter styler = ConverterFactory.getConverter();
    // TransformingSAXEventProvider htmlsep = (TransformingSAXEventProvider) styler.convert(osissep);

    // You can also pass parameters to the XSLT. What you pass depends upon what the XSLT can use.
    BookMetaData bmd = book.getBookMetaData();
    boolean direction = bmd.isLeftToRight();
    htmlsep.setParameter("direction", direction ? "ltr" : "rtl"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    // Finally you can get the styled text.
    return XMLUtil.writeToString(htmlsep);
  }



  def handleProtocol = {
    def protocol = params.protoc?.trim().replace(":", "")
//       println "pro:"+protocol
    def data = params.key
   // println "proto:" + protocol
   // println " key:" + data
    def result
    try {
      if (protocol.equals(GREEK_DEF_PROTOCOL)) {
        result = jump(Defaults.getGreekDefinitions(), data);
      }
      else if (protocol.equals(HEBREW_DEF_PROTOCOL)) {
        result = jump(Defaults.getHebrewDefinitions(), data);
      }
      else if (protocol.equals(GREEK_MORPH_PROTOCOL)) {
          result = jump(Defaults.getGreekParse(), data);
        }
        else if (protocol.equals(HEBREW_MORPH_PROTOCOL)) {
            result = jump(Defaults.getHebrewParse(), data);
          }
          else if (protocol.equals(DICTIONARY_PROTOCOL)) {
              result = jump(Defaults.getDictionary(), data);
            }
            else {
              result = "no data"
            }
    }
    catch (NoSuchKeyException ex) {
      ex.printStackTrace()
    }
    if (result == null) {
      result = new HashMap()
      result.put("result", "no data")
    }
    //render result as JSON

  }
  /**
   * Open the requested book and go to the requested key.
   * @param book The book to use
   * @param data The key to find
   */
  def jump(Book book, String data) {
    def text
    if (book != null && Books.installed().getBook(book.getName()) != null) {


      text = readStyledText(book.getInitials(), data, new Integer(0), new Integer(300))
      /* if (!(data.startsWith("H")||data.startsWith("G"))){
           text+=readStyledText(book.getInitials(), "G"+data, new Integer(0), new Integer(300))
             text+=readStyledText(book.getInitials(), "H"+data, new Integer(0), new Integer(300))
      }*/
    }

    // println "data: " + data
    //println "txt:" + text
    Map rslt = new HashMap()
    rslt.put("result", text)
    rslt
  }


  def flipStrongs = {
    def state_strongs = session.state_strongs
    if (state_strongs == null) {
      state_strongs = "false"
    }
    if (state_strongs.equals("false")) {
      state_strongs = "true"
    } else {
      state_strongs = "false"
    }
    session.state_strongs = state_strongs
    redirect(action: reflush, params: params)

  }

  def flipMorph = {
    def state_morph = session.state_morph
    if (state_morph == null) {
      state_morph = "false"
    }
    if (state_morph.equals("false")) {
      state_morph = "true"
    } else {
      state_morph = "false"
    }
    session.state_morph = state_morph
    redirect(action: reflush, params: params)
  }

  def flipVline = {
    def state_vline = session.state_vline
    if (state_vline == null) {
      state_vline = "false"
    }
    if (state_vline.equals("false")) {
      state_vline = "true"
    } else {
      state_vline = "false"
    }
    session.state_vline = state_vline
    redirect(action: reflush, params: params)
  }


  public void doVLine(boolean toggle) {
    XSLTProperty.START_VERSE_ON_NEWLINE.setState(toggle);
  }

  public void doVNum() {
    XSLTProperty.VERSE_NUMBERS.setState(true);
    XSLTProperty.CV.setState(false);
    XSLTProperty.BCV.setState(false);
    XSLTProperty.NO_VERSE_NUMBERS.setState(false);
  }

  public void doTinyVNum(boolean toggle) {
    XSLTProperty.TINY_VERSE_NUMBERS.setState(toggle);
  }

  public void doBCVNum() {
    XSLTProperty.VERSE_NUMBERS.setState(false);
    XSLTProperty.CV.setState(false);
    XSLTProperty.BCV.setState(true);
    XSLTProperty.NO_VERSE_NUMBERS.setState(false);
  }

  public void doCVNum() {
    XSLTProperty.VERSE_NUMBERS.setState(false);
    XSLTProperty.CV.setState(true);
    XSLTProperty.BCV.setState(false);
    XSLTProperty.NO_VERSE_NUMBERS.setState(false);
  }

  public void doNoVNum() {
    XSLTProperty.VERSE_NUMBERS.setState(false);
    XSLTProperty.CV.setState(false);
    XSLTProperty.BCV.setState(false);
    XSLTProperty.NO_VERSE_NUMBERS.setState(true);
  }

  public void doHeadings(boolean toggle) {
    XSLTProperty.HEADINGS.setState(toggle);
  }

  public void doNotes(boolean toggle) {
    XSLTProperty.NOTES.setState(toggle);
  }

  public void doXRef(boolean toggle) {
    XSLTProperty.XREF.setState(toggle);
  }

  String day() {
    def dt = new Date();
    java.text.DateFormat dateFormat = new SimpleDateFormat("MM.dd")
    dateFormat.format(dt)
  }

def dumpwholebible(version){
	def bibles=  	updateBibles(Locale.CHINA) 
	def params=new HashMap()
	params.put("version","kjv")	
	params.put("book","Joh")	
	params.put("chapter","3")	
	params.put("verse","16")
    	binding['jswordService']=new BibleService()
//def s=	read (bibles,params)
//println s
  binding['xslurl'] = ResourceUtil.getResource("iBD_cmnd.xsl");
  //binding['xslurl'] = ResourceUtil.getResource("iBD_cmnd_strongs.xsl");
    List books = Books.installed().getBooks(BookFilters.getBibles());
//version="ChiUNS"	
//version="KJV"	
def writer = new File("bible_${version}.xml").newWriter("UTF-8", true)
writer.write("""<?xml version="1.0" encoding="utf-8"?>""")
def xml = new MarkupBuilder(writer)
def idex=1
def versionobj=  findVersion(books, version)
xml.bible(translation:version){
//Dump whole Bible into one xml
bibles.each{bible->
  def nchapters=findChapters(bible.key) 
 book(name:bible.key,tc:nchapters){
  (1..nchapters).each{chap->

  def nVerses = findVerses(bible.key, chap)
 chapter(chn:chap,tv:nVerses){
     (1..nVerses).each{ver->
            def ref = bible.key + " " + chap + ":" + ver
            def searchresult = searchByRef(versionobj?.initials?: "ChiUns", ref)// "dis this verse " + ver
            verse(ref:ref,vn:ver,id:idex++){
//		span(class:'verse',id:ver,ver)
		txt(searchresult.get("data"))}
  }
writer.flush()
}
}

}
}
}
}
def dumpwholedictionary(version){
	def bibles=  	updateBibles(Locale.CHINA) 
	def params=new HashMap()
    	binding['jswordService']=new BibleService()
  binding['xslurl'] = ResourceUtil.getResource("iBD_cmnd.xsl");
    List books = Books.installed().getBooks(BookFilters.getDictionaries());
    def data= jswordService.getOSISString(version, params.key, 0, 10)
def writer = new File("dictionary_${version}.xml").newWriter("UTF-8", true)
writer.write("""<?xml version="1.0" encoding="utf-8"?>""")
def xml = new MarkupBuilder(writer)
def idex=1
def dict=  findVersion(books, version)
Key keys=dict.getGlobalKeyList()
def iter=keys.iterator()

xml.dictionary(name:version){
while (iter.hasNext()){
 Key ky=iter.next()
 BookData dta=new BookData(dict,ky)
 def txt=OSISUtil.getPlainText(dta.getOsisFragment())
   term(ky,txt) 	
}}
writer.flush()
}
def membible(version){
	def bibles=  	updateBibles(Locale.CHINA) 
	def params=new HashMap()
	params.put("version","kjv")	
	params.put("book","Joh")	
	params.put("chapter","3")	
	params.put("verse","16")
    	binding['jswordService']=new BibleService()
//def s=	read (bibles,params)
//println s
  binding['xslurl'] = ResourceUtil.getResource("iBD_cmnd_noref.xsl");
  //binding['xslurl'] = ResourceUtil.getResource("iBD_cmnd_strongs.xsl");
    List books = Books.installed().getBooks(BookFilters.getBibles());
//version="ChiUNS"	
//version="KJV"	
def writer = new File("membible_${version}.xml").newWriter("UTF-8", true)
writer.write("""<?xml version="1.0" encoding="utf-8"?>""")
def xml = new MarkupBuilder(writer)
def idex=1
def versionobj=  findVersion(books, version)
xml.bible(translation:version){
new File("../../web-app/memb/membiblesch.txt").splitEachLine(";"){
            def ref = it[2].trim()
            def searchresult = searchByRef(versionobj?.initials?: "ChiUns", ref)
//		if(searchresult) verse(ref:ref,id:idex++,txt:searchresult.get("data"))
		if(searchresult) verse(ref:ref,id:idex++,txt:searchresult.get("data").replaceAll("<(.|\n)*?>", ''))
  }
}
writer.flush()
}
def fetchVerses(version,ref){
	def bibles=  	updateBibles(Locale.CHINA) 
	def params=new HashMap()
	params.put("version","kjv")	
	params.put("book","Joh")	
	params.put("chapter","3")	
	params.put("verse","16")
    	binding['jswordService']=new BibleService()
  //binding['xslurl'] = ResourceUtil.getResource("iBD_cmnd_noref.xsl");
  binding['xslurl'] = ResourceUtil.getResource("iBD_noref.xsl");
    List books = Books.installed().getBooks(BookFilters.getBibles());
def idex=1
def versionobj=  findVersion(books, version)
            def searchresult = searchByRef(versionobj?.initials?: "ChiUns", ref)
		searchresult.get("data").replaceAll("<(.|\n)*?>", '')
}

def uploadbible(version){
	def bibles=  	updateBibles(Locale.CHINA) 
	def params=new HashMap()
	params.put("version","kjv")	
	params.put("book","Joh")	
	params.put("chapter","3")	
	params.put("verse","16")
    	binding['jswordService']=new BibleService()
def baseurl="http://localhost:8080/uploadBible.groovy?"
  binding['xslurl'] = ResourceUtil.getResource("iBD_cmnd.xsl");
  //binding['xslurl'] = ResourceUtil.getResource("iBD_cmnd_strongs.xsl");
    List books = Books.installed().getBooks(BookFilters.getBibles());
//version="ChiUNS"	
//version="KJV"	
def idex=1
def versionobj=  findVersion(books, version)
bibles.each{bible->
  def nchapters=findChapters(bible.key) 
  (1..nchapters).each{chap->
  def nVerses = findVerses(bible.key, chap)
     (1..nVerses).each{ver->
            def ref = bible.key + " " + chap + ":" + ver
            def searchresult = searchByRef(versionobj?.initials?: "ChiUns", ref)// "dis this verse " + ver
		def text=searchresult.get("data")
	def urlpath=baseurl+"version=${version}"+"&book=${bible.key}"+"&chapter=${chap}"+"&verse=${ver}"
def url=	new URL(urlpath+"&id=${idex++}&text=${URLEncoder.encode(text,'UTF8')}")
def conn=url.openConnection()
	conn.doOutput=true
	conn.outputStream<<"BROL"
	conn.inputStream.eachLine{ println it}	
println idex
  }
}
}

}

def indexbible(version){
	def bibles=  	updateBibles(Locale.CHINA) 
    	binding['jswordService']=new BibleService()
def baseurl="http://localhost:8080/uploadBibleindex.groovy?"
  binding['xslurl'] = ResourceUtil.getResource("iBD_cmnd.xsl");
  //binding['xslurl'] = ResourceUtil.getResource("iBD_cmnd_strongs.xsl");
    List books = Books.installed().getBooks(BookFilters.getBibles());
//version="ChiUNS"	
//version="KJV"	
def idex=1
def versionobj=  findVersion(books, version)
def indexes=new HashMap()
bibles.each{bible->
  def nchapters=findChapters(bible.key) 
  (1..nchapters).each{chap->
  def nVerses = findVerses(bible.key, chap)
     (1..nVerses).each{ver->
	def key=idex++
        def ref = bible.key + " " + chap + ":" + ver
        def searchresult = searchByRef(versionobj?.initials?: "ChiUns", ref)
	def text=searchresult.get("data").replaceAll("<(.|\n)*?>", '')
        text.split(" ").each{xword->
	def word=xword?.trim().replaceAll(",","").replaceAll(";","").replaceAll(" ","").replaceAll(":","")
	if (word){
		if (indexes.containsKey(word)){
				indexes.get(word).add(key)
			}else{
				def box=new HashSet()
				box.add(key)
				indexes.put(word,box)
			}
	}
	} 
}
}
}
	indexes.keySet().each{wd->
	def urlpath=baseurl+"version=${version}"+"&key=${URLEncoder.encode(wd,'UTF8')}"+"&index=${indexes.get(wd).join(',')}"
def url=new URL(urlpath)
def conn=url.openConnection()
	conn.doOutput=true
	conn.outputStream<<"BROL"
	conn.inputStream.eachLine{ println it}	

	}
}
def cleanText(source){
//source?.replaceAll("</w:t>","").replaceAll("</w:r>","").replaceAll("<w:r>","").replaceAll("<w:rPr>","").replaceAll("</w:rPr>","").replaceAll("""<w:rFonts w:hint="eastAsia"/>""","").replaceAll("<w:t>","").replaceAll("""<w:t xml:space="preserve">""","")

      source?.replaceAll(/\<(.*?)\>/,"")?.replaceAll("\n"," ").replaceAll("\r"," ")
}
def processScript(rawtxt,version){

      rawtxt.replaceAll(/\((.*?)\)/){fullmatch,txt->
	def result=new StringBuilder()
	println "ref:"+txt
	txt.split(";").each{scpt->	
	try{
	result<<fetchVerses(version,cleanText(scpt))+" "
	}catch(Exception e){
		println "verse:"+scpt
		e.printStackTrace()
	}
	}
	return result.toString()+ " ($txt)"
            }

}
def splitfile(rawtxt,version){

      rawtxt.replaceAll(/\((.*?)\)/){fullmatch,txt->
	def result=new StringBuilder()
	println "ref:"+txt
	txt.split(";").each{scpt->	
	try{
	result<<fetchVerses(version,cleanText(scpt))+" "
	}catch(Exception e){
		println "verse:"+scpt
	}
	}
	return result.toString()+ " ($txt)"
            }

}
def getTargetEncoding(version){
	if (version=="ChiNCVs"||version=="ChiUns") return "GB2312"
	else return "UTF-8"
}
def translateBible(sourcefile,destination,version){
def txt=new File(sourcefile).getText("UTF-8")
def result=""
try{
result=processScript(txt,version)
}catch (Exception e){
	println "problem with $version"
}
try{
def targetencoding=getTargetEncoding(version)
def writer = new File(destination).newWriter(targetencoding, true)
writer.write(result.replace("macintosh",targetencoding))
writer.close()
}catch (Exception e){
}
}

def splittranslateBible(sourcefile,destination,version){
def rawtxt=new File(sourcefile).getText("UTF-8")
(0..10).each{day->
	def token="daily:"+day
      rawtxt.eachMatch(/${token}(.*?)${token}/){fullmatch,txt->
	def result=processScript(txt,version)
try{
def writer = new File(destination+"/day"+day+".xml").newWriter("UTF-8", true)
writer.write(result.replace("macintosh","UTF-8"))
writer.close()
}catch (Exception e){ }
	}
}
}

def extractmonth(source, start, end){
def b=source?.indexOf(start)+start?.size()
def e=source?.lastIndexOf(end)
println start +"b:"+b+"---"+end+" e:"+e
if (e>b)return source.substring(b,e)
else return null
}
def extractday(source, start, end){
def b=source?.indexOf(start)+start?.size()
def e=source?.indexOf(end)
println start +"b:"+b+"---"+end+" e:"+e
if (e>b)return source.substring(b,e)
else return null
}
def splittodaily(heads,tails,sourcefile,destination){
def rawtxt=new File(sourcefile).getText("UTF-8")
def header=new File(heads).getText("UTF-8")
def tail=new File(tails).getText("UTF-8")
int i=0
if(rawtxt)
(0..4).each{month->
	def key="MONTH:${month}"
      def monthtxt=extractmonth(rawtxt,key,key)
if(monthtxt)
(1..32).each{day->
	def tokenb="DAY "+day
	def tokene="DAY "+(day+1)
	def txt=extractday(monthtxt,tokenb,tokene)
if(txt)
try{
def writer=null
writer = new File(destination+"/day"+ i++ +".htm").newWriter("UTF-8", true)
writer.write(header+txt+tail)
writer.close()
}catch (Exception e){ 
//e.printStackTrace()
	}
finally{

}
}
}
}
//version="ChiUNS"	
//dumpwholebible(version)
//membible(version)
//uploadbible(version)
//indexbible(version)
//dumpwholedictionary('hebrewstrongcn')

def sources=["Chinese_Handbook_to_Prayer_v2"
//"Handbook_to_Prayer"
//"/Users/yiguanghu/ccim/prayerministry/daily/day0"
//,"Handbook_to_Renewal"
//"/Users/yiguanghu/Downloads/The_Story_Ch7"
]
/*
sources.each{source->
try{
translateBible(source+".xml",source+"_ChiUnt.xml","ChiNCVt")
translateBible(source+".xml",source+"_ChiUns.xml","ChiUns")
translateBible(source+".xml",source+"_ChiNCVs.xml","ChiNCVs")
translateBible(source+".xml",source+"_ChiNCVt.xml","ChiNCVt")
}catch (Exception e){
}
}
*/
def sourceroot="/Users/yiguanghu/ccim/prayerministry/daily/day"
def dest="tk"
(2..0).each{day->
translateBible(sourceroot+day+".htm",dest+day+"_ChiNCVt.html","ChiNCVt")
translateBible(sourceroot+day+".htm",dest+day+"_ChiNCVs.html","ChiNCVs")
translateBible(sourceroot+day+".htm",dest+day+"_ChiUns.html","ChiUns")
}
//splittranslateBible("/Users/yiguanghu/ccim/prayerministry/daily/Skeleton.xml","/Users/yiguanghu/ccim/prayerministry/daily/tmp","ChiNCVt")
/***
***/
//translateBible("/Users/yiguanghu/ccim/prayerministry/daily/Skeleton.xml","/Users/yiguanghu/ccim/prayerministry/daily/Skeleton_ChiNCVt.xml","ChiNCVt")
//splittodaily("/Users/yiguanghu/ccim/prayerministry/daily/header.htm","/Users/yiguanghu/ccim/prayerministry/daily/tail.htm","/Users/yiguanghu/ccim/prayerministry/daily/Skeleton_ChiNCVt.htm","/Users/yiguanghu/ccim/prayerministry/daily/tmp/")
//for Timmy

sourceroot="/Users/yiguanghu/timmy/"
sources=["Prayers of Affirmation",
"Prayers of Closing",
"Prayers of Confession",
"Prayers of Intercession",
"Prayers of Petition",
"Prayers of Renewal",
"Prayers of Thanksgiving"]
sources.each{fil->
translateBible(sourceroot+fil+".xml",sourceroot+fil+"_ChiNCVT.xml","ChiNCVt")
//translateBible(sourceroot+fil+".xml",sourceroot+fil+"_Chiuntxin.xml","chiuntxin")
}
