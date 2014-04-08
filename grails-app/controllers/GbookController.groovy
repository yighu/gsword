import org.crosswire.common.xml.*
import java.io.*;
import groovy.xml.MarkupBuilder
import org.crosswire.jsword.book.*
import java.net.MalformedURLException;
import java.net.URL;
import com.google.gson.*;
import org.crosswire.jsword.book.sword.SwordBookPath
import org.crosswire.jsword.index.IndexManagerFactory
import org.crosswire.jsword.passage.Key
import org.crosswire.jsword.passage.NoSuchKeyException
import org.crosswire.jsword.passage.Passage
import org.crosswire.common.util.Translations;
import org.crosswire.bibledesktop.passage.KeyTreeModel
import org.crosswire.jsword.util.ConverterFactory
import org.crosswire.jsword.versification.BibleInfo
import org.crosswire.jsword.bridge.BookInstaller
import org.xml.sax.ContentHandler
import org.xml.sax.SAXException
import grails.converters.*
import org.crosswire.common.util.NetUtil;
import org.crosswire.common.util.ResourceUtil;
import java.text.SimpleDateFormat;
import org.crosswire.jsword.book.Defaults;
import org.crosswire.bibledesktop.desktop.XSLTProperty
import org.crosswire.jsword.versification.BibleNames
import org.apache.poi.hslf.usermodel.SlideShow
import org.apache.poi.hslf.model.Slide
import org.apache.poi.hslf.model.TextBox
import org.apache.poi.hslf.usermodel.RichTextRun
import org.springframework.web.context.request.RequestContextHolder
import javax.servlet.http.HttpSession;

class GbookController {
  private static final String BIBLE_PROTOCOL = "bible";                                     //$NON-NLS-1$
  private static final String DICTIONARY_PROTOCOL = "dict";                                      //$NON-NLS-1$
  private static final String GREEK_DEF_PROTOCOL = "gdef";                                      //$NON-NLS-1$
  private static final String HEBREW_DEF_PROTOCOL = "hdef";                                      //$NON-NLS-1$
  private static final String GREEK_MORPH_PROTOCOL = "gmorph";                                    //$NON-NLS-1$
  private static final String HEBREW_MORPH_PROTOCOL = "hmorph";                                    //$NON-NLS-1$
  private static final String COMMENTARY_PROTOCOL = "comment";                                   //$NON-NLS-1$
  private static final String STRONGS_NUMBERS = "Strongs"; //$NON-NLS-1$
  private static final String NOTES= "notes"; //$NON-NLS-1$
  private static final String HEADINGS= "Headings"; //$NON-NLS-1$
  private static final String XREF= "XRef"; //$NON-NLS-1$
  def jswordService
  def languageService
  //def scaffold = Gbook
  def index = {
    redirect(action: v)
  }
  def beforeInterceptor = {
    def key = "org.springframework.web.servlet.DispatcherServlet.LOCALE_RESOLVER"
    def localeResolver = request.getAttribute(key)
    Locale locale = localeResolver.resolveLocale(request)

    if (locale != null) {
      localeResolver.setLocale(request, response, locale)
      Locale.setDefault(locale);
      String langename = locale.getDisplayLanguage()
      //println " language=" + langename
      if (langename.equals("English")) {
        if (session.englishbibles == null) {

          updateBibles(Locale.US)
          session.englishbibles = bibles
          session.englishbiblekeymap = biblekeymap

        } else {
/*
          if(!session.englishbibles) {
		updateBibles(Locale.US)
          session.englishbibles = bibles
          session.englishbiblekeymap = biblekeymap
		}
*/
          bibles = session.englishbibles
          biblekeymap = session.englishbiblekeymap

        }
      } else{

        if (session.chinesebibles == null) {

          updateBibles(Locale.CHINA)

          session.chinesebibles = bibles
          session.chinesebiblekeymap = biblekeymap

        } else {
/*
          if(!session.chinesebibles) {
		updateBibles(Locale.CHINA)
          session.chinesebibles = bibles
          session.chinesebiblekeymap = biblekeymap
		}
*/
          bibles = session.chinesebibles
          biblekeymap = session.chinesebiblekeymap
        }
      }
      session.currentlaunage = langename

    }

  }
  def static final DELIM = ";"
  //def static final BR=" " //HORIZON TOC
  def static final BR="<br/>" //vertical TOC
  private printkey(Key key, int level, String base, String book) {
    int cn = key.getChildCount();
    StringBuffer result = new StringBuffer()
    
    for (int i = 0; i < cn; i++) {
      def k = key.get(i)
      def indntb = new StringBuffer("")
      for (int l = 0; l < level; l++) {
        indntb.append("&nbsp;&nbsp;&nbsp;&nbsp;")
      }
      //def indnt = indntb.toString() //for vertical toc
      def indnt = " " //For horizontal TOC
      if (k.getChildCount() > 1) {
        result.append(indnt + k.getName() + BR + printkey(k, level + 1, base + "${DELIM}" + k.getName(), book))
      } else {
        //  println "xorgs:"+ k.getName()

        // println "replaced:"+convertNonAscii(k.getName())

        def kk = base + DELIM + k.getName()

        result.append(indnt + "<a href=\"/gsword/gbook/gentxt?key=${kk.encodeAsHTML()}\">" + k.getName() + "</a>"+BR)
      }
    }

    result.toString()
  }
  private printkey_dropdown(Key key, int level, String base, String book) {
    int cn = key?.getChildCount();
	List result=new ArrayList()
    //StringBuffer result = new StringBuffer()

    for (int i = 0; i < cn; i++) {
      def k = key?.get(i)
      if (k?.getChildCount() > 1) {
        result.addAll( printkey_dropdown(k, level + 1, base + "${DELIM}" + k.getName(), book))
      } else {
        def kk = base + DELIM + k?.getName()
        //result.add(kk.encodeAsHTML())
        result.add(kk)
      }
    }

    result
  }

private convertNonAscii(String s) {
    def PLAIN_ASCII = "AaEeIiOoUuAaEeIiOoUuYyAaEeIiOoUuYyAaOoNnAaEeIiOoUuYyAaCcOoUu"
    def UNICODE = "\u00C0\u00E0\u00C8\u00E8\u00CC\u00EC\u00D2\u00F2\u00D9\u00F9\u00C1\u00E1\u00C9\u00E9\u00CD\u00ED\u00D3\u00F3\u00DA\u00FA\u00DD\u00FD\u00C2\u00E2\u00CA\u00EA\u00CE\u00EE\u00D4\u00F4\u00DB\u00FB\u0176\u0177\u00C3\u00E3\u00D5\u00F5\u00D1\u00F1\u00C4\u00E4\u00CB\u00EB\u00CF\u00EF\u00D6\u00F6\u00DC\u00FC\u0178\u00FF\u00C5\u00E5\u00C7\u00E7\u0150\u0151\u0170\u0171"
    if (s == null) return null;
    StringBuffer sb = new StringBuffer();
    int n = s.length();
    for (int i = 0; i < n; i++) {
      char c = s.charAt(i);
      int pos = UNICODE.indexOf(new String(c));
      if (pos > -1) {
        sb.append(PLAIN_ASCII.charAt(pos));
      }
      else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  def gentxt = {
    def layers = params.key?.decodeHTML().split("${DELIM}")
    def nl = layers.size()
    Book book = jswordService.getBook(layers[0]);
    Key key = book.getGlobalKeyList();
    Key wk = key
    for (int i = 1; i < nl; i++) {
      def nc = wk.getChildCount();
      for (int j = 0; j < nc; j++) {
        if (wk.get(j).getName()?.trim().equals(layers[i]?.trim()?.replaceAll("\u00E2\u0080\u0094", "\u2014"))) {
          wk = wk.get(j)
          break;
        }
      }
    }
    def toc = layers[0] + "<br/><hr/>" + genToc(layers[0])
    def result = readStyledText(layers[0], wk)
    List books = Books.installed().getBooks(BookFilters.getGeneralBooks())
    List dropdowntoc= new ArrayList()
	dropdowntoc.addAll(genToc_dropdown(layers[0]))
    render(view: 'generalbooks', model: [books: books, txt: result, toc: toc,gendropdowntoc:dropdowntoc])

  }
  def gentxtremote = {
    def layers = params.key?.decodeHTML().split("${DELIM}")
    def nl = layers.size()
    Book book = jswordService.getBook(layers[0]);
    Key key = book.getGlobalKeyList();
    Key wk = key
    for (int i = 1; i < nl; i++) {
      def nc = wk.getChildCount();
      for (int j = 0; j < nc; j++) {
        if (wk.get(j).getName()?.trim().equals(layers[i]?.trim()?.replaceAll("\u00E2\u0080\u0094", "\u2014"))) {
          wk = wk.get(j)
          break;
        }
      }
    }
    //def toc = new ArrayList()
//	toc.addAll(genToc_dropdown(layers[0]))
    def result = readStyledText(layers[0], wk)
    //List books = Books.installed().getBooks(BookFilters.getGeneralBooks())
    //render(view: 'generalbooks', model: [books: books, txt: result, toc: toc])
    def data=new HashMap();
    //	data.put("books",books)
	data.put("data",result)
//	data.put("toc",toc)	
	render data as JSON
  }

  private genChildren(String reference) {
    //println "refernce "+reference
    def layers = reference.split(":")
    // println "lev:"+layers.size();

    Book book = jswordService.getBook(layers[0]);
    Key key = book.getGlobalKeyList();
    Key k = key;
    for (int i = 1; i < layers.size(); i++) {

      key.each {c ->
        if (c.name == layers[i]) {
          k = c
        } else {
          println c.name + " is not " + layers[i]
        }

      }
      key = k
    }
    //println "found "+key.name
    layers.each {layer ->
      println layer
    }
  }

  private genToc_dropdown(String initials) {
    if (!initials) initials = "Institutes"
    Book book = jswordService.getBook(initials);
    Key key = book?.getGlobalKeyList();
    printkey_dropdown(key, 0, initials, initials)
  }
  private genToc(String initials) {
    if (!initials) initials = "Institutes"
    Book book = jswordService.getBook(initials);
    Key key = book.getGlobalKeyList();
    printkey(key, 0, initials, initials)
  }

  private genToc(String[] initials) {
    if (initials.length <= 0) {
      return ""
    }
    else {
      return genToc(initials[0])
    }
  }

  def c= {
    def rst = ""
    if (!params.id) {
      params.id = "Institutes"
    }
    rst = params.id + "<br/><hr/>" + genToc(params.id)
    def result = ""
    if (params.ref) {
      result = readStyledText(params.id, params.ref, 0, 2000)
    }
    List books = Books.installed().getBooks(BookFilters.getGeneralBooks())
    List dropdowntoc= new ArrayList()
	dropdowntoc.addAll(genToc_dropdown(params.id))
    render(view: 'generalbooks', model: [books: books, txt: result, toc: rst,gendropdowntoc:dropdowntoc])

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
/*
  def twitterService
  def twitterupdate = {
    if (params.user == "yighu") {
      twitterService.update()
    }

  }
  def membible = {
    if (params.user == "yighu") {
      twitterService.membible()
    }

  }
  def ontwitter = {
    def content = twitterService.getTimeline()
    Map map = new HashMap();
    map.put("data", content)

    render map as JSON
  }
*/
  def searchDictionarykey = {
    // def keys = Person.findAllByNameLike("%${params.query}%")
    def qry = params.query
    Book bookc = BookInstaller.getInstalledBook("ZhHanzi");
    Key kc = bookc.getGlobalKeyList()
    render(contentType: "text/xml") {
      results {
        kc.each {k ->
          result {
            name(k.name)
            id(k.name)
          }
        }
      }
    }
  }
  def v = {

//    Book.metaClass.localename = {->
//
//      return delegate.name + "loc"
//    }
    //  println "lang:" + params.lang
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
    def limit=200
    if (params.lmt){
      limit=Integer.parseInt(params.lmt);
      if (limit<0 || limit>200)limi=200
    }
    //controller: 'gbook', action: 'display', params: '\'bible=\' + escape(bible)+\'&key=\'+escape(reference)+\'&start=\'+verseStart+\'&limit=\'+verseLimit',
    //  println "version:" + version
    // println "key:" + key
    //println "start:" + start

    def result
	try{
	result = readStyledText(version, key, start, limit)
	}catch (Exception e){
	println ("exception get styledtext:"+version +" ky:"+key)
//	e.printStackTrace()
	}
    def total 
	try{
	total= jswordService.getCardinality(version, key)
	}catch (Exception e){
	println ("cardinality:"+version+" ky:"+key)
//	e.printStackTrace()
}

    List books = Books.installed().getBooks(BookFilters.getBibles());
    List dictionaries = Books.installed().getBooks(BookFilters.getDictionaries());
    List commentaries = Books.installed().getBooks(BookFilters.getCommentaries());
    List devotions = Books.installed().getBooks(BookFilters.getDailyDevotionals());
    def bibles = getBibles(session)
    def bible = "KJV"
    def chapters = jswordService.getChapters(bible);
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
//    println "bibles:"
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
    def oneyearbible = {

    def version = params.version
    if (!version) {
      version = "ChiUns"
    }

     session.state_headings= "true"
     session.state_notes="true"
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


    def result
	try{
	result = readStyledText(version, key, start, 200)
	}catch (Exception e){
println ("exception get ${version} ${key}	")
	}
    def total
	try{
	total= jswordService.getCardinality(version, key)
	}catch (Exception e){
println ("exception total get ${version} ${key}	")
}

    List books = Books.installed().getBooks(BookFilters.getBibles());
    List dictionaries = Books.installed().getBooks(BookFilters.getDictionaries());
    List commentaries = Books.installed().getBooks(BookFilters.getCommentaries());
    List devotions = Books.installed().getBooks(BookFilters.getDailyDevotionals());
    def bibles = getBibles(session)
    def bible = "KJV"
    def chapters = jswordService.getChapters(bible);
    def mainbooks = new ArrayList()
    mainbooks.add("ChiUns")
    mainbooks.add("ChiUn")
    mainbooks.add("ChiNCVs")
    mainbooks.add("ChiNCVt")
    mainbooks.add("KJV")
    // mainbooks.add("ESV")
    render(view: 'oneyearbible', model: [results: result, ref: key, version: version, total: total, mainbooks: mainbooks, books: books, dictionaries: dictionaries, commentaries: commentaries, bibles: bibles, chapters: chapters, devotions: devotions])
  }
    def dailydevotions= {

    def version = params.version
    if (!version) {
      version = "ChiUns"
    }
     session.state_headings= "true"
     session.state_notes="true"

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


    def result
	try{
	result = readStyledText(version, key, start, 200)
	}catch (Exception e){
	println "exception in result: version:"+version +" key:"+key
	}
    def total
	try{
	total= jswordService.getCardinality(version, key)
	}catch (Exception e){
	println "exception in total: version:"+version +" key:"+key
}

    List books = Books.installed().getBooks(BookFilters.getBibles());
    List dictionaries = Books.installed().getBooks(BookFilters.getDictionaries());
    List commentaries = Books.installed().getBooks(BookFilters.getCommentaries());
    List devotions = Books.installed().getBooks(BookFilters.getDailyDevotionals());
    def bibles = getBibles(session)
    def bible = "KJV"
    def chapters = jswordService.getChapters(bible);
    def mainbooks = new ArrayList()
    mainbooks.add("ChiUns")
    mainbooks.add("ChiUn")
    mainbooks.add("ChiNCVs")
    mainbooks.add("ChiNCVt")
    mainbooks.add("KJV")
    // mainbooks.add("ESV")
    render(view: 'daily', model: [results: result, ref: key, version: version, total: total, mainbooks: mainbooks, books: books, dictionaries: dictionaries, commentaries: commentaries, bibles: bibles, chapters: chapters, devotions: devotions])
  }
  def listkey = {
    //  println "dci="+ params

    Book book = BookInstaller.getInstalledBook("Easton");
    Key key = null;
    
    key = book.getKey(params.query + "~");
    def c = key.cardinality
    // for (int i=0;i<c;i++){
    // println "k "+key.get(i)
    //}
    //  key.each {k->
    //  println  k
    //}
//Create XML response
    // println "key;"+key
    Iterator iter = key.iterator();
    /* while (iter.hasNext()) {
         def k=iter.next()
      println k

    }*/
    render(contentType: "text/xml") {
      results {
        while (iter.hasNext()) {
          def k = iter.next()
          // println k
          result {
            name(k)
            id(k)
          }
        }
      }
    }
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
    //   println "version:" + params.version
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
    try{
    FileOutputStream out = new FileOutputStream(grailsApplication.config.docroot + "/" + session.id + ".ppt");
    // File fl=new File(session.id + ".ppt")
    // println fl.getAbsolutePath()
    //FileOutputStream out = new FileOutputStream(fl);
    ppt.write(out);
    out.close();
	}catch (Exception e){
	println "failed save:"+session.id
	}

    Map map = new HashMap();
    map.put("data", session.id + ".ppt")
    render map as JSON
  }

  def memv={

  def d=jswordService.memVersecurrent(params.b?:"kjv"); 
    Map map = new HashMap();
    map.put("data", d)
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

        if (book.getLanguage().getName().equals("Chinese")) {
          gv.text = (OSISUtil.getCanonicalText(data.getOsisFragment()))?.trim()?.replaceAll(" ", "")?.replaceAll("\t", "").replaceAll("\r", "").replaceAll("\n", "")

        } else {
          try {
            gv.text = OSISUtil.getCanonicalText(data.getOsisFragment())
          } catch (Exception e) {
	println "exception in plaintext: bookinit:"+bookInitials+" ref:"+reference
          }
        }

        gv.name = vs.getName()
        gv.cbook = retrivebookfromreference(vs.getName(), bookInitials, book.getLanguage()?.getName(), session)

        result.add(gv)
      }
    }



    return result
  }



  private retrivebookfromreference(String reference, String bookInitials, String language, HttpSession session) {
    def x = jswordService.fetchbook(reference)
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

  def todayskey(){
      def scheules = getSchedule()
      def td = today()
      def k=scheules.get(td)
	if (k) {
	return k
	}else{
	return "John 3:15"
	}
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
    if(params.key){
		key=params.key
	}else if (!(params.book || params.chapter || params.verse)) {
      def scheules = getSchedule()
      def td = today()

      key = todayskey()
    }else {
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
//    println "version:" + version
    //  println "key:" + key
    // println "start:" + start
    if (key==null && session.prevkey)key=session.prevkey
    if (key==null)key = todayskey()
	session.prevkey=key
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
 //   println "in search Bible :key:"+  params.key +" bible:"+params.bible
    def ref = jswordService.search(params.bible, params.key)
//	println "ref:"+ref
    def text = readStyledText(params.bible, ref, 0, 10)
//	println "text:"+text

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
  def getChaps = {

    int bn = BibleInfo.getBookNumber(params.bible);
	if(bn<1)bn=1
    int cn = BibleInfo.chaptersInBook(bn);
    
    Map mp = new HashMap();
    mp.put("nchaps", cn)
    render mp as JSON
  }
  def display = {

    def text = readStyledText(params.bible, params.key, Integer.parseInt(params.start), Integer.parseInt(params.limit))

    def total = jswordService.getCardinality(params.bible, params.key)

    Map mp = new HashMap();
    mp.put("data", text)
    mp.put("verses", params.key)
    mp.put("total", total)
    render mp as JSON
  }
  def rnd = {
    def data
    def reader
    try {
      URL url = new URL("http://localhost:8080/gsword/gbook/randomVerseJSON");
      reader = new BufferedReader(new InputStreamReader(url.openStream()));
      JsonParser jp = new JsonParser()
      JsonElement je = jp.parse(reader)
      data = je.getAt("data");
      reader.close()

    } catch (MalformedURLException e) {
      // ...
    } catch (IOException e) {
      // ...
    } finally {
      reader.close()
    }
    render data;
  }
  def randomVerseJSON = {
    def verse = jswordService.randomVerse()
    def data = jswordService.getPlainText("KJV", verse)
    Map mp = new HashMap()
    mp.put("data", verse+":"+data)
    render mp as JSON
  }
    def randomVerseText = {
    def verse = jswordService.randomVerse()
    render verse+":"+jswordService.getPlainText("KJV", verse)
  }
  def display4g = {
    def result = getPlainText(params.bible, params.key, 20, session)
    StringBuffer sb = new StringBuffer()
    result?.each {

      sb.append("\r\n" + it.cbook?.trim() + "" + it.chapter + ":" + it.verse + " " + it.text)
    }
    Map mp = new HashMap();
    mp.put("data", sb.toString())
    mp.put("verses", params.key)
    mp.put("total", result?.size())
    render mp as JSON
  }
  def getReference = {

    def ref = jswordService.search(params.bible, params.key)
    render(view: 'index', model: [data: ref])
  }
  def searchDictionary = {
   def dic
	if (params.key){
      params.dic=languageService.therightdic(params.dic,params.key)
    //dic = jswordService.getOSISString(params.dic, params.key, 0, 10)
    dic= readStyledText(params.dic, params.key, 0, 10)
	}
    def mp = new HashMap()
    mp.put("data", dic)

    render mp as JSON
  }
  def searchdics={
    def bookkey
    def keyword
    def keyvalue=""
    if (params.dic){

        bookkey = BookInstaller.getInstalledBook(params.dic)?.getGlobalKeyList();
       if (params.offset) {
         keyword=bookkey.get(Integer.parseInt(params.offset)).getName()
        // println "keyword:"+keyword
        // languageService.detect(keyword)
         params.dic=languageService.therightdic(params.dic,keyword)
         //printlnb "dic:"+params.dic
         //keyvalue = jswordService.getOSISString(params.dic, keyword, 0, 10)
    	 keyvalue= readStyledText(params.dic, keyword, 0, 10)
       }

    }
    [dictionaries:Books.installed().getBooks(BookFilters.getDictionaries()),dic:params.dic,bookkey:bookkey,keyword:keyword,keyvalue:keyvalue]
  }
  def suggestedkeys={
   def keys= jswordService.listkey(params.dependsOnValue?:"Easton", params.query)
    keys=keys.collect {
      [id:it,name:it]
    }
	if (keys?.size()<=0){
	keys=new ArrayList()
	}
	keys.add([id:params.query,name:params.query])
    def jsonkeys=[
            keys:keys
    ]
  render jsonkeys as JSON
  }
  def getOsis = {

    //  println params.bible + " " + params.reference + " " + params.start + " " + params.limit
    def text = readStyledText(params.bible, params.reference, Integer.parseInt(params.start?:"1"), Integer.parseInt(params.limit))
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

  def diclook = {
    List dictionaries = Books.installed().getBooks(BookFilters.getDictionaries());
    render(view: 'diclook', model: [dictionaries: dictionaries])

  }

  def contactus = {
    render(view: 'contact')
  }
  def messagebox= {
	println params.msg
	println params.title
    render(view: 'msgbox',model:[msg:params.msg])
   }
  def sendmail = {
    // println params.name+  " "+params.email+" "+params.comment
    try {
      sendMail {
        to "yiguang.hu@gmail.com"
        from params.email
        subject "Comment on GSword"
        body " From " + params.name + "\n" + params.comment
      }
    } catch (Exception e) {
      //  e.printStackTrace()
    }
    render "Thanks For Contacting Us"
  }
  public static final dailyschedule = new HashMap()
  private static final URL scheduletxt = ResourceUtil.getResource("daily.txt");

  private getSchedule() {
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
  def yearbook={
	yeartext(params.book)
	}
  private yeartext(book){

    def dt = new Date();
    java.text.DateFormat dateFormat = new SimpleDateFormat("MMdd")
def writer = new File("${book}.xml").newWriter("UTF-8", true)
writer.write("""<?xml version="1.0" encoding="utf-8"?>""")
def xml = new MarkupBuilder(writer)
	
xml.records() {
    (1..365).each{
	dt.putAt(6,it)
    	def td=dateFormat.format(dt)
    	def txt=readStyledText(book, td, 0, 500)
	entry(day:td,value:txt)
	}
	}
	}
   def completebook={
		wholebook(params.book)
	}
  def wholebook(book){

def writer = new File("${book}.xml").newWriter("UTF-8", true)
writer.write("""<?xml version="1.0" encoding="utf-8"?>""")
def xml = new MarkupBuilder(writer)
def bbs=["gen","exo"] as List
xml.records() {
  bbs.each{bible->
	bible{	
    (1..365).each{chapter->
		chapter{
		(1..300).each{verse->
    	def txt=readStyledText(book, '${bible} ${chapter}:${verse}', 0, 1)
		if (txt) verse(id:verse,txt)
		}
		}
		}
		}
	}
	}
	}
  private dailytxt(String book) {
    def scheules = getSchedule()
    def td = today()
      session.state_vline = "true"
    readStyledText(book, scheules.get(td), 0, 500)
  }

  private dailytxt() {
    def scheules = getSchedule()
    def td = today()
      session.state_vline = "true"
    readStyledText(params.bible ?: "KJV", scheules.get(td), 0, 500)
  }

  def daily = {
    def text = dailytxt();
    def mp = new HashMap()
    mp.put("data", text)
    render mp as JSON
  }

  def private bibles = new ArrayList()
  def private biblekeymap = new HashMap()

  private getBibles(HttpSession session) {
    /*      def locale=session.currentlocale
    if (bibles.isEmpty()) {
      println "locale "+locale
      println " bible is empty. update locale default"
      updateBibles(locale)
    }else{
       updateBibles(locale)
    }*/
    if (session.currentlaunage.equals("English")) {
      return session.englishbibles
    } else {
      return session.chinesebibles

    }

    return session.chinesebibles
  }
  
  def getEnglishShortnames(){
      int nbook = BibleInfo.booksInBible();
      	BibleNames bn = new BibleNames(Locale.US)
	def names=new ArrayList()
	names.add("")
        for (int i = 1; i <= nbook; i++)
          names.add(BibleInfo.getBookName(i).getShortName())
	names
	}

def shortnames_eng=[" ","Gen",
"Exo",
"Lev",
"Num",
"Deu",
"Josh",
"Judg",
"Ruth",
"1Sam",
"2Sam",
"1Ki",
"2Ki",
"1Ch",
"2Ch",
"Ezr",
"Neh",
"Est",
"Job",
"Psa",
"Pro",
"Ecc",
"Song",
"Isa",
"Jer",
"Lam",
"Eze",
"Dan",
"Hos",
"Joe",
"Amo",
"Obd",
"Jon",
"Mic",
"Nah",
"Hab",
"Zep",
"Hag",
"Zec",
"Mal",
"Mat",
"Mar",
"Luk",
"Joh",
"Act",
"Rom",
"1Cor",
"2Cor",
"Gal",
"Eph",
"Phili",
"Col",
"1Th",
"2Th",
"1Ti",
"2Ti",
"Titus",
"Phil",
"Heb",
"Jam",
"1Pe",
"2Pe",
"1Jo",
"2Jo",
"3Jo",
"Jude",
"Rev"] as List

  private  updateBibles(Locale loc) {
    bibles.clear()
    biblekeymap.clear()
    //  println "update bible for locale " + loc
    if (bibles.isEmpty()) {

      int nbook = BibleInfo.booksInBible();
       //def shortnames= shortnames_eng;
      //  println "the locla:" + loc
      BibleNames bn = new BibleNames(loc)

      try {
        for (int i = 1; i <= nbook; i++)

        {

          def gb = new Expando()
          gb.key = shortnames_eng.get(i)
	  gb.shortkey=BibleInfo.getBookName(i).getShortName()
	  //println "key:"+gb.key +" shortkey:"+gb.shortkey
          gb.shortname = bn.getShortName(i)
          gb.cname = bn.getName(i)
          gb.longname = BibleInfo.getBookName(i).getLongName()
          biblekeymap.put(gb.longname, gb.cname)
          biblekeymap.put(gb.shortname, gb.cname)
          biblekeymap.put(gb.key, gb.cname)

          bibles.add(gb)
        }


      } catch (Exception e) {
	println ("Update bible failed for loc:"+loc)
      }
    }
    return bibles
  }

  //  private static final     URL xslurl = ResourceUtil.getResource("xsl/cswing/simple.xsl");
  private static final URL xslurl = ResourceUtil.getResource("iBD.xsl");

private String readStyledText(String bookInitials, String reference, int start, int maxKeyCount) throws NoSuchKeyException, BookException, SAXException {
    def mainbook = "KJV";
    try{
    mainbook = bookInitials.split(',')[0];
    }catch (Exception e){
	println "exception in readStyledText: bookinit:"+bookInitials+" ref:"+reference
    }
	println "here1"
    Book book = jswordService.getBook(mainbook);
    SAXEventProvider osissep 
    try{
	osissep= jswordService.getOSISProvider(bookInitials, reference, start, maxKeyCount);
	}catch (Exception e){
	println ("get osisprovide fail for:"+bookInitials+" ref: "+reference)
	}
    if (osissep == null) {
      return ""; //$NON-NLS-1$
    }

	println "here2"
    //  println "use new style....." +xslurl.getPath()
    TransformingSAXEventProvider htmlsep = new TransformingSAXEventProvider(NetUtil.toURI(xslurl), osissep); //Customize xslt
    //  doStrongs(false)

    if (!session.state_strongs) {
      session.state_strongs = "false"
    }
    htmlsep.setParameter(STRONGS_NUMBERS, session.state_strongs)
    if (!session.state_notes) {
      session.state_notes= "false"
    }
    htmlsep.setParameter(NOTES, session.state_notes)
    if (!session.state_headings) {
      session.state_headings= "false"
    }
    htmlsep.setParameter(HEADINGS, session.state_headings)
    if (!session.state_xref) {
      session.state_xref= "false"
    }
    htmlsep.setParameter(XREF, session.state_xref)

    if (!session.state_morph) {
      session.state_morph = "false"
    }
    htmlsep.setParameter("Morph", session.state_morph)

    if (!session.state_vline) {
      session.state_vline = "true"

    }
    htmlsep.setParameter("VLine", session.state_vline)

	println "here3"
    //Converter styler = ConverterFactory.getConverter();
    // TransformingSAXEventProvider htmlsep = (TransformingSAXEventProvider) styler.convert(osissep);

    // You can also pass parameters to the XSLT. What you pass depends upon what the XSLT can use.
    BookMetaData bmd = book.getBookMetaData();
    boolean direction = bmd.isLeftToRight();
    htmlsep.setParameter("direction", direction ? "ltr" : "rtl"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	println "here4"
	println "here5"
    // Finally you can get the styled text.
    return XMLUtil.writeToString(htmlsep);
  }


  private String readStyledText(String bookInitials, Key key) throws NoSuchKeyException, BookException, SAXException {
    Book book = jswordService.getBook(bookInitials);
    SAXEventProvider osissep = jswordService.getOSISProvider(bookInitials, key);
    if (osissep == null) {
      return ""; //$NON-NLS-1$
    }
    TransformingSAXEventProvider htmlsep = new TransformingSAXEventProvider(NetUtil.toURI(xslurl), osissep); //Customize xslt
    BookMetaData bmd = book.getBookMetaData();
    boolean direction = bmd.isLeftToRight();
    htmlsep.setParameter("VLine", "true")

    htmlsep.setParameter("direction", direction ? "ltr" : "rtl"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    return XMLUtil.writeToString(htmlsep);
  }



  def handleProtocol = {
    def protocol = params.protoc?.trim()?.replace(":", "")
//       println "pro:"+protocol
    def data = params.key
    //  println "proto:" + protocol
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
      println ("handle protol:"+protocol)
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

  def flipXRef= {
    def state_xref= session.state_xref
    if (state_xref== null) {
      state_xref= "false"
    }
    if (state_xref.equals("false")) {
      state_xref= "true"
    } else {
      state_xref= "false"
    }
    session.state_xref= state_xref
    redirect(action: reflush, params: params)

  }
  def flipHeadings= {
    def state_headings= session.state_headings
    if (state_headings== null) {
      state_headings= "false"
    }
    if (state_headings.equals("false")) {
      state_headings= "true"
    } else {
      state_headings= "false"
    }
    session.state_headings= state_headings
    redirect(action: reflush, params: params)

  }
  def flipNotes= {
    def state_notes= session.state_notes
    if (state_notes== null) {
      state_notes= "false"
    }
    if (state_notes.equals("false")) {
      state_notes= "true"
    } else {
      state_notes= "false"
    }
    session.state_notes= state_notes
    redirect(action: reflush, params: params)

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
    //def dic = jswordService.getOSISString(params.devotion, day(), 0, 10)
    def dic= readStyledText(params.devotion, day(), new Integer(0), new Integer(300))

    def mp = new HashMap()
    mp.put("data", dic)

    render mp as JSON
  }

  def dailydevotion = {
    // println params.devotion
    //def dic = jswordService.getOSISString(params.devotion, day(), 0, 10)
    def dic= readStyledText(params.devotion, day(), new Integer(0), new Integer(300))

    def mp = new HashMap()
    mp.put("data", dic)

    render mp as JSON
  }
  def feed = {
    def strms = jswordService.getOSISString("STREAMSS", day(), 0, 10)
    if (!strms) {
      strms = ""
    }
    def sme = jswordService.getOSISString("SME", day(), 0, 10)
    def dbd = jswordService.getOSISString("DBD", day(), 0, 10)
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

   def feedService
    def feeds = {
      Integer id=0
      try{
        id=Integer.parseInt(params.id)
      } catch(Exception e){
       // e.printStackTrace()
        id=-1
      }
       render(view: 'feed', model: [meditates: feedService.getGswordnow(),membibles:feedService.getMembiblenow(id)])
    }

}


