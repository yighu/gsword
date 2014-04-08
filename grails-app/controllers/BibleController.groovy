import grails.converters.JSON
import java.text.SimpleDateFormat
import javax.servlet.http.HttpSession
import javax.xml.transform.TransformerException
import org.apache.poi.hslf.model.Slide
import org.apache.poi.hslf.model.TextBox
import org.apache.poi.hslf.usermodel.RichTextRun
import org.apache.poi.hslf.usermodel.SlideShow
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

class BibleController {
  //static navigation = true
  private static final String BIBLE_PROTOCOL = "bible";                                     //$NON-NLS-1$
  private static final String DICTIONARY_PROTOCOL = "dict";                                      //$NON-NLS-1$
  private static final String GREEK_DEF_PROTOCOL = "gdef";                                      //$NON-NLS-1$
  private static final String HEBREW_DEF_PROTOCOL = "hdef";                                      //$NON-NLS-1$
  private static final String GREEK_MORPH_PROTOCOL = "gmorph";                                    //$NON-NLS-1$
  private static final String HEBREW_MORPH_PROTOCOL = "hmorph";                                    //$NON-NLS-1$
  private static final String COMMENTARY_PROTOCOL = "comment";                                   //$NON-NLS-1$
  private static final String STRONGS_NUMBERS = "Strongs"; //$NON-NLS-1$
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
  
//@Validateable
private findVersion(List books, String version) {
    // println "v sssion "+version
    def rst
    books?.each {book ->
      if (book.initials.equalsIgnoreCase(version)) {
        rst = book
      }
    }
    // println " rst:"+rst
    rst
  }

  private findBible(List bibles, String name) {
    def rst
    bibles.each {bible ->

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
  def read = {
    session.state_vline=true
    List books = Books.installed().getBooks(BookFilters.getBibles());
    def bibles = getBibles(session)

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
            render(view: "read", model: [books: books, book: versionobj,layer:layer, bibles: bibles, bible: bok, chapters: chapters, chap: chap, verses: verses, txt: searchresult.get("data")]) //for one chapter

          } else {
            def ref = bok.key + " " + chap
            def searchresult = searchByRef(versionobj.initials, ref)// "dis this verse " + ver

            render(view: "read", model: [books: books, book: versionobj, layer:layer,bibles: bibles, bible: bok, chapters: chapters, chap: chap, verses: verses, txt: searchresult.get("data")]) //for one chapter

          }

        } else {
          def ref = bok?.key +" 1"
          def searchresult = searchByRef(versionobj.initials, ref)// "dis this verse " + ver
          render(view: "read", model: [books: books, book: versionobj, layer:layer,bibles: bibles, bible: bok, chapters: chapters, txt: searchresult.get("data")]) //for one chapter

        }
      } else {
        def ref = " John 1"
        def searchresult = searchByRef(versionobj?.initials, ref)// "dis this verse " + ver
        render(view: "read", model: [books: books, book: versionobj, layer:layer,bibles: bibles, txt: searchresult.get("data")])   //  one version
      }
    } else {
      def ref = "Gen John 1"
      def searchresult = searchByRef("kjv", ref)// "dis this verse " + ver
      render(view: "read", model: [books: books, layer:layer,txt: searchresult.get("data")])            //all versions
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
  def private static final B4C="ChiUns,KJV,"

  private retrivekeybyid(String version, int offset){
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
 private retrivekeyindex(String version, String key){
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

  private createSlide_oneText(Slide s1ide, String text) {
    // TextBox title = s1ide.addTitle();

    //title.setText(it.get("key"));
    TextBox shape = new TextBox();
    RichTextRun rt = shape.getTextRun().getRichTextRuns()[0];
    // def ref = search(params.version, params.key)
    //println " bibe:"+params.version +" key:"+params.key  +" ref:"+ref

    /*   shape.setText(
"January\r" +
    "February\r" +
    "March\r" +
    "April");*/
    shape.setText(text)
    rt.setFontSize(42);

    rt.setBullet(true);
    rt.setBulletOffset(0);  //bullet offset
    rt.setTextOffset(50);   //text offset (should be greater than bullet offset)
//         rt.setBulletChar("\u263A"); //bullet character
    s1ide.addShape(shape);

    shape.setAnchor(new java.awt.Rectangle(50, 20, 650, 650));  //position of the text box in the slide
    s1ide.addShape(shape);
  }

private createSlide(Slide s1ide, String text) {
    // TextBox title = s1ide.addTitle();

    //title.setText(it.get("key"));
    TextBox shape = new TextBox();
    RichTextRun rt = shape.getTextRun().getRichTextRuns()[0];
    // def ref = search(params.version, params.key)
    //println " bibe:"+params.version +" key:"+params.key  +" ref:"+ref

    /*   shape.setText(
"January\r" +
    "February\r" +
    "March\r" +
    "April");*/
    shape.setText(text)
    rt.setFontSize(42);

    rt.setBullet(true);
    rt.setBulletOffset(0);  //bullet offset
    rt.setTextOffset(50);   //text offset (should be greater than bullet offset)
//         rt.setBulletChar("\u263A"); //bullet character
    s1ide.addShape(shape);

    shape.setAnchor(new java.awt.Rectangle(50, 0, 650, 650));  //position of the text box in the slide
    s1ide.addShape(shape);
  }

  def ppt = {
    SlideShow ppt = new SlideShow();
    if (params.version?.equals("KJV")) {
      if (session.englishbibles == null) {
        updateBibles(Locale.US)
        session.englishbibles = bibles
        session.englishbiblekeymap = biblekeymap

      }
      bibles = session.englishbibles
      biblekeymap = session.englishbiblekeymap

    } else {
      if (session.chinesebibles == null) {

        updateBibles(Locale.CHINA)
        session.chinesebibles
        session.chinesebiblekeymap = biblekeymap

      }
      bibles = session.chinesebibles
      biblekeymap = session.chinesebiblekeymap

    }
   // println "version:" + params.version
    def list = getPlainText(params.version, params.key, 200, session)
    String text = ""
    String previousbook = ""
    int bk = 0
    int cp = 0

    list.each {

      /* if (it.book == bk && it.chapter == cp) {
      text += it.verse + " " + it.text
    } else */
      //{
      bk = it.book
      cp = it.chapter

      text += "\r" + it.cbook?.trim() + "" + it.chapter + ":" + it.verse + " " + it.text

      //}
      if (text.length() > 70) {
        Slide s1ide = ppt.createSlide();
        createSlide(s1ide, text)
        text = ""
        cp = 0
        bk = 0

      }

    }
    if (text.length() > 2) {
      Slide s1ide = ppt.createSlide();
      createSlide(s1ide, text)
    }
    FileOutputStream out = new FileOutputStream(grailsApplication.config.docroot + "/ppt/" + session.id + ".ppt");
    // File fl=new File(session.id + ".ppt")
    // println fl.getAbsolutePath()
    //FileOutputStream out = new FileOutputStream(fl);
    ppt.write(out);
    out.close();

    //   println " session:" + session.id
    Map map = new HashMap();
    map.put("data", session.id + ".ppt")

    render map as JSON
  }


  def excl = {
    if (params.version?.equals("KJV")) {
      if (session.englishbibles == null) {
        updateBibles(Locale.US)
        session.englishbibles = bibles
        session.englishbiblekeymap = biblekeymap

      }
      bibles = session.englishbibles
      biblekeymap = session.englishbiblekeymap

    } else {
      if (session.chinesebibles == null) {

        updateBibles(Locale.CHINA)
        session.chinesebibles
        session.chinesebiblekeymap = biblekeymap

      }
      bibles = session.chinesebibles
      biblekeymap = session.chinesebiblekeymap

    }
    def list = getPlainText(params.version, params.key, 200, session)
    response.setHeader("Content-Disposition", "attachment; filename=verse.xls")
    render(contentType: "application/vnd.ms-excel") {
      html {
        body {
          h1("Bible Verses")
          table {
            tr {
              th("Ref")
              th("text")
            }
            for (b in list) {
              tr {
                td(b.cbook?.trim() + "" + b.chapter + ":" + b.verse + " ")
                td(b.text)
              }
            }
          }
        }
      }
    }


  }

  private getPlainText(String bookInitials, String reference, int maxKeyCount, HttpSession session) throws BookException, NoSuchKeyException {
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


  private fetchbook(String refernce) {
    def digit = 0
    for (int counter = 0; counter < refernce.length(); counter++) {
      if (Character.isDigit(refernce.charAt(counter))) {
        digit = counter
        break
      }

    }
    refernce.substring(0, digit).trim()
  }

  private retrivebookfromreference(String reference, String bookInitials, String language, HttpSession session) {
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

  private SAXEventProvider getOSIS(String bookInitials, String reference, int maxKeyCount) throws BookException, NoSuchKeyException {
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

  def reflush = {
   // println "lang:" + params.lang
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
   // println "version:" + version
    //println "key:" + key
    //println "start:" + start

    def text = readStyledText(version, key, start, 200)
    Map mp = new HashMap();
    mp.put("data", text)

    render mp as JSON

  }

  def nbibles = {
    //org.crosswire.jsword.versification.
    //  BibleNames bn = new BibleNames(Locale.CHINA)
    BibleNames bn = new BibleNames()

    /* for (int i = 1; i <= 66; i++) {
        println bn.getShortName(i) + "/" + bn.getLongName(i);
    }*/
  }

  def searchBible = {

    Book bible = Books.installed().getBook(params.bible)
    // println "key:"+  params.key
    def ref = jswordService.search(params.bible, params.key)
    def text = readStyledText(params.bible, ref, 0, 10)

    def total = jswordService.getCardinality(params.bible, ref)
    //  println ref
    Map mp = new HashMap();
    mp.put("data", text)
    mp.put("verses", ref)
    mp.put("total", total)
    render mp as JSON
    // render(view: 'index', model: [data: text,reference:ref])
    //return text
  }

  private searchByRef(String version, String ref) {
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

  private searchByKey(String version, String key) {

    def ref = jswordService.search(version, key)
    def text = readStyledText(version, ref, 0, 10)
    def total = jswordService.getCardinality(version, ref)
    Map mp = new HashMap();
    mp.put("data", text)
    mp.put("verses", ref)
    mp.put("total", total)
    mp
  }
  private searchByKey(String version, String key,int offset) {
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
  def getChaps = {
    int cn = findChapters(params.bible)
    Map mp = new HashMap();
    mp.put("nchaps", cn)
    render mp as JSON
  }

  private findChapters(String bible) {
    int bn = BibleInfo.getBookNumber(bible);
    if (bn<1)bn=1
    BibleInfo.chaptersInBook(bn);

  }

  private findVerses(String bible, int chapter) {
    int bn = BibleInfo.getBookNumber(bible);
    //BibleInfo.chaptersInBook(bn);
	if (chapter<1)chapter=1
	if(bn<1)bn=1
    BibleInfo.versesInChapter(bn, chapter)
  }

  def display = {
    //   println params.bible
    // println params.key
    def text = readStyledText(params.bible, params.key, Integer.parseInt(params.start), Integer.parseInt(params.limit))

    def total = jswordService.getCardinality(params.bible, params.key)

    Map mp = new HashMap();
    mp.put("data", text)
    mp.put("verses", params.key)
    mp.put("total", total)
    render mp as JSON
    // render(view: 'index', model: [data: text,reference:ref])
    //return text
  }
  def getReference = {

    def ref = jswordService.search(params.bible, params.key)
    render(view: 'index', model: [data: ref])
  }
  def searchDictionary = {
    def dic = getOSISString(params.dic, params.key, 0, 10)
    def mp = new HashMap()
    mp.put("data", dic)

    render mp as JSON
  }
  def getOsis = {

    //  println params.bible + " " + params.reference + " " + params.start + " " + params.limit
    def text = readStyledText(params.bible, params.reference, Integer.parseInt(params.start), Integer.parseInt(params.limit))
    def total = jswordService.getCardinality(params.bible, params.reference)
    Map mp = new HashMap();
    mp.put("data", text)
    mp.put("total", total)
    render mp as JSON

  }
  def init = {
    redirect(action: v)
/*
        List books = Books.installed().getBooks(BookFilters.getBibles());
        List dictionaries = Books.installed().getBooks(BookFilters.getDictionaries());
        List commentaries = Books.installed().getBooks(BookFilters.getCommentaries());
        def bibles = getBibles()
        def bible = "KJV"
        def chapters = getChapters(bible);
        def mainbooks = new ArrayList()
        mainbooks.add("ChiUns")
        mainbooks.add("ChiUn")
        mainbooks.add("ChiNCVs")
        mainbooks.add("ChiNCVt")
        mainbooks.add("KJV")
        mainbooks.add("ESV")
        render(view: 'bible', model: [mainbooks: mainbooks, books: books, dictionaries: dictionaries, commentaries: commentaries, bibles: bibles, chapters: chapters])

  */
  }
  def adsearch = {
    def books = org.crosswire.bibledesktop.book.Msg.PRESETS.toString().split("\\|")
    render(view: 'adsearch', model: [books: books])

  }
  def static final dailyschedule = new HashMap()
  private static final URL scheduletxt = ResourceUtil.getResource("daily.txt");

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

  private dailytxt(String book) {
    def scheules = getSchedule()
    def td = today()
    readStyledText(book, scheules.get(td), 0, 500)
  }

  private dailytxt() {
    def scheules = getSchedule()
    def td = today()
    readStyledText(params.bible ?: "KJV", scheules.get(td), 0, 500)
  }

  def daily = {
    def text = dailytxt();
    def mp = new HashMap()
    mp.put("data", text)
    render mp as JSON
    //render(view: 'bible', model: [dailyword:text,mainbooks: mainbooks, books: books, dictionaries: dictionaries, commentaries: commentaries, bibles: bibles, chapters: chapters])


  }

  def private bibles = new ArrayList()
  def private biblekeymap = new HashMap()

  private getBibles(HttpSession session) {

    if (session.currentlaunage.equals("English")) {
      return session.englishbibles
    } else {
      return session.chinesebibles

    }

    return session.chinesebibles
  }

  private updateBibles(Locale loc) {
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
  private static final URL xslurl = ResourceUtil.getResource("iBD.xsl");

  private String readStyledText(String bookInitials, String reference, int start, int maxKeyCount) throws NoSuchKeyException, BookException, TransformerException, SAXException {
    def mainbook = "KJV";
   try{
    mainbook = bookInitials.split(',')[0];
   }catch (Exception e){
     println "book ini"+bookInitials
   }
    if(!bookInitials)bookInitials="KJV"
    println "main book:"+mainbook
    Book book = jswordService.getBook(mainbook);
    SAXEventProvider osissep = jswordService.getOSISProvider(bookInitials, reference, start, maxKeyCount);
    if (osissep == null) {
      return ""; //$NON-NLS-1$
    }

    //  println "use new style....." +xslurl.getPath()
    TransformingSAXEventProvider htmlsep = new TransformingSAXEventProvider(NetUtil.toURI(xslurl), osissep); //Customize xslt
    //  doStrongs(false)

    if (!session.state_strongs) {
      session.state_strongs = "false"
    }
    htmlsep.setParameter(STRONGS_NUMBERS, session.state_strongs)

    if (!session.state_morph) {
      session.state_morph = "false"
    }
    htmlsep.setParameter("Morph", session.state_morph)

    if (!session.state_vline) {
      session.state_vline = "false"

    }
    htmlsep.setParameter("VLine", session.state_vline)

    //Converter styler = ConverterFactory.getConverter();
    // TransformingSAXEventProvider htmlsep = (TransformingSAXEventProvider) styler.convert(osissep);

    // You can also pass parameters to the XSLT. What you pass depends upon what the XSLT can use.
    BookMetaData bmd = book.getBookMetaData();
    boolean direction = bmd.isLeftToRight();
    htmlsep.setParameter("direction", direction ? "ltr" : "rtl"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    // Finally you can get the styled text.
    return XMLUtil.writeToString(htmlsep);
  }


  def jswordService

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
    render result as JSON

  }
  /**
   * Open the requested book and go to the requested key.
   * @param book The book to use
   * @param data The key to find
   */
  private jump(Book book, String data) {
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


  private void doVLine(boolean toggle) {
    XSLTProperty.START_VERSE_ON_NEWLINE.setState(toggle);
  }

  private void doVNum() {
    XSLTProperty.VERSE_NUMBERS.setState(true);
    XSLTProperty.CV.setState(false);
    XSLTProperty.BCV.setState(false);
    XSLTProperty.NO_VERSE_NUMBERS.setState(false);
  }

  private void doTinyVNum(boolean toggle) {
    XSLTProperty.TINY_VERSE_NUMBERS.setState(toggle);
  }

  private void doBCVNum() {
    XSLTProperty.VERSE_NUMBERS.setState(false);
    XSLTProperty.CV.setState(false);
    XSLTProperty.BCV.setState(true);
    XSLTProperty.NO_VERSE_NUMBERS.setState(false);
  }

  private void doCVNum() {
    XSLTProperty.VERSE_NUMBERS.setState(false);
    XSLTProperty.CV.setState(true);
    XSLTProperty.BCV.setState(false);
    XSLTProperty.NO_VERSE_NUMBERS.setState(false);
  }

  private void doNoVNum() {
    XSLTProperty.VERSE_NUMBERS.setState(false);
    XSLTProperty.CV.setState(false);
    XSLTProperty.BCV.setState(false);
    XSLTProperty.NO_VERSE_NUMBERS.setState(true);
  }

  private void doHeadings(boolean toggle) {
    XSLTProperty.HEADINGS.setState(toggle);
  }

  private void doNotes(boolean toggle) {
    XSLTProperty.NOTES.setState(toggle);
  }

  private void doXRef(boolean toggle) {
    XSLTProperty.XREF.setState(toggle);
  }

  String day() {
    def dt = new Date();
    java.text.DateFormat dateFormat = new SimpleDateFormat("MM.dd")
    dateFormat.format(dt)
  }

  def devotion = {
    // println params.devotion
    def dic = getOSISString(params.devotion, day(), 0, 10)
  //    println "dic "+dic
    def mp = new HashMap()
    mp.put("data", dic)

    render mp as JSON
  }

  def dailydevotion = {
    // println params.devotion
    def dic = getOSISString(params.devotion, day(), 0, 10)

    def mp = new HashMap()
    mp.put("data", dic)

    render mp as JSON
  }
  def feed = {
    def strms = getOSISString("STREAMSS", day(), 0, 10)
    if (!strms) {
      strms = ""
    }
    def sme = getOSISString("SME", day(), 0, 10)
    def dbd = getOSISString("DBD", day(), 0, 10)
    def chiuns = dailytxt("ChiUns")
    def kjv = dailytxt("KJV")
    def ncvs = dailytxt("ChiNCVs")
    //   def title=""
    //def link=""
    //def description=""
    render(feedType: "rss", feedVersion: "2.0") {
      title = "GSword Daily Devotion"
      link = "http://rock.ccim.org/gsword/gbook/feed"
      description = "GSword Daily Devotion"
      entry("Streams in the Desert") {
        title = "Streams in the Desert"
        link = "http://rock.ccim.org/gsword/gbook/v"
        strms
      }
      entry("Day By Day By Grace") {
        title = "Day By Day By Grace"
        link = "http://rock.ccim.org/gsword/gbook/v"
        dbd
      }
      entry("C. H. Spurgeon Morning and Evening: Daily Readings") {
        title = "C. H. Spurgeon Morning and Evening: Daily Readings"
        link = "http://rock.ccim.org/gsword/gbook/v"
        sme
      }
      entry("One Year Bible (Simplified Chinese Union)") {
        title = "One Year Bible (Simplified Chinese Union)"
        link = "http://rock.ccim.org/gsword/gbook/v"
        chiuns
      }
      entry("One Year Bible (Simplified New Chinese Version)") {
        title = "One Year Bible (Simplified New Chinese Version)"
        link = "http://rock.ccim.org/gsword/gbook/v"
        ncvs
      }
      entry("One Year Bible (King James Version)") {
        title = "One Year Bible (King James Version)"
        link = "http://rock.ccim.org/gsword/gbook/v"
        kjv
      }
    }
  }
  def twitterService
 def twitterupdate={
    twitterService.update()
 }
  def ontwitter={
    def content=twitterService.fetchTimeline()
    render content 
  }
def bookindexService
 def doindex={
	bookindexService.doIndex()
}
}


