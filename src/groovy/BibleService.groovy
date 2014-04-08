import org.crosswire.common.xml.*
import org.crosswire.jsword.book.*
import org.crosswire.jsword.book.sword.SwordBookPath
import org.crosswire.jsword.index.IndexManagerFactory
import org.crosswire.jsword.passage.Key
import org.crosswire.jsword.passage.NoSuchKeyException
import org.crosswire.jsword.passage.Passage
import org.crosswire.common.util.Translations;
import groovy.sql.Sql;
import org.crosswire.jsword.util.ConverterFactory
import org.crosswire.jsword.versification.BibleInfo
import org.crosswire.jsword.bridge.BookInstaller
import org.xml.sax.ContentHandler
import org.xml.sax.SAXException
import grails.converters.*
import javax.xml.transform.TransformerException
import org.crosswire.common.util.NetUtil;
import org.crosswire.common.util.ResourceUtil;
import java.text.SimpleDateFormat;
import org.crosswire.jsword.book.Defaults;
import org.crosswire.bibledesktop.desktop.XSLTProperty
import org.crosswire.jsword.versification.BibleNames

class BibleService {

  boolean transactional = false

  /**
   * Determine whether the named book can be searched, that is, whether
   * the book is indexed.
   *
   * @param bookInitials the named book to check.
   * @return true if searching can be performed
   */
  public boolean isIndexed(String bookInitials) {
    return isIndexedb(BookInstaller.getInstalledBook(bookInitials));
  }


  def processLine(String line) {
    line
  }
  def lastid_daily(){
      def config = ConfigurationHolder.config
    def membibleid

    try{
   membibleid= new File(config.membibleid).text;
    }catch(Exception e){
      membibleid="0"
    }
    if (!membibleid) {
      membibleid = "0"
    }
    membibleid;
  }
  def memVersecurrent(String b) {
    def config = ConfigurationHolder.config
    def membibleid=lastid_daily()

    def verse
    try{
    new File(config.membiblesch).eachLine {line ->
      if (membibleid-- == 0) {
        verse= line.split(";")[2]        
      }
    }
	}catch (Exception e){}
    if(!verse)verse="john 3:15"
     verse+" "+ getPlainText(b, verse.trim())
  }
  def memVersecurrentplusprv(String b,Integer index) {
//        println "do "+b
    def config = ConfigurationHolder.config
    def membibleid=index;
    if (membibleid<=0){
    membibleid=Integer.parseInt((lastid_daily()).trim())
    }
    def result=new ArrayList()
    def verse
    try{
    new File(config.membiblesch).eachLine {line ->
      if (membibleid>=0 && membibleid-- < 8) {
        verse= line.split(";")[2]
      // println "do "+verse  +" for id "+ membibleid
        if(!verse)verse="john 3:15"
        result.add(verse+"[chiuns]"+ getPlainText("chiuns", verse.trim())+"<br/>"+verse+"[kjv]   "+ getPlainText("kjv", verse.trim())+"<br/>")
       // result.append(verse+" "+ getPlainText(b, verse.trim())+"<br/>")
      }
    }
	}catch (Exception e){
      e.printStackTrace()
    }
//     result
    def nr=new ArrayList()
     int len=result.size()
     for (int i=len-1;i>0;i--){
        if (i==len-1){
        nr.add(result.get(i)+"<br/> <b>Review previous</b><br/> ")
        }else{
          nr.add(result.get(i))
        }
     }
    nr
  }


  def memVerse() {
    def config = ConfigurationHolder.config
    def membibleid=lastid_daily()

    def next = Integer.parseInt(membibleid?.trim()) + 1;
    new File(config.membibleid).write(""+next) 
    def verse
    new File(config.membiblesch).eachLine {line ->
      if (next-- == 0) {
        verse= line.split(";")[2]        
      }
    }
   // println verse
    verse?.trim()
  }

  def randomVerse() {
    def random = new Random()
    def verse = random.nextInt(BibleInfo.versesInBible()) + 1
    int ct = 0
    def result
    for (bn in 1..66) {
      if (result == null) {
        def book = BibleInfo.getShortBookName(bn)
        def cn = BibleInfo.chaptersInBook(bn)
        for (ch in 1..cn) {
          if (result == null) {
            def vn = BibleInfo.versesInChapter(bn, ch)
            for (i in 1..vn) {
              ct++
              if (ct++ >= verse) {
                result = book.toString() + " " + ch + ":" + i
                break
              }
            }
          } else {
            break
          }
        }
      } else {
        break
      }
    }
    result
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

  /**
   * Get a reference list for a search result against a book.
   *
   * @param bookInitials
   * @param searchRequest
   * @return The reference for the matching.
   * @throws BookException
   */
  public String search(String bookInitials, String searchRequest) throws BookException {
	println "in jsword search:"+bookInitials +" req:"+searchRequest
      def rst = ""
    Book book = BookInstaller.getInstalledBook(bookInitials);
    if (isIndexedb(book) && searchRequest != null) {
	println "xin jsword search:"+bookInitials +" indexed"
      if (BookCategory.BIBLE.equals(book.getBookCategory())) {
        BibleInfo.setFullBookName(false);
      }
      try {
        rst = book.find(searchRequest).getName();
      } catch (Exception e) {
		e.printStackTrace()
      }
      return rst
    }
    return ""; //$NON-NLS-1$
  }

  /**
   * Get close matches for a target in a book whose keys have a meaningful sort. This is not true of
   * keys that are numeric or contain numbers. (unless the numbers are 0 filled.)
   */
  public String[] match(String bookInitials, String searchRequest, int maxMatchCount) {
    Book book = BookInstaller.getInstalledBook(bookInitials);
    if (book == null || searchRequest == null || maxMatchCount < 1) {
      return new String[0];
    }

    // Need to use the locale of the book so that we can find stuff in the proper order
    Locale sortLocale = new Locale(book.getLanguage().getCode());
    String target = searchRequest.toLowerCase(sortLocale);

    // Get everything with target as the prefix.
    // In Unicode \ uFFFF is reserved for internal use
    // and is greater than every character defined in Unicode
    String endTarget = target + '\ufffe';

    // This whole getGlobalKeyList is messy.
    // 1) Some drivers cache the list which is slow.
    // 2) Binary lookup would be much better.
    // 3) Caching the whole list here is dumb.
    // What is needed is that all this be pushed into JSword proper.
    // TODO(dms): Push this into Book interface.
    List result = new ArrayList();
    Iterator iter = book.getGlobalKeyList().iterator();
    int count = 0;
    while (iter.hasNext()) {
      Key key = (Key) iter.next();
      String entry = key.getName().toLowerCase(sortLocale);
      if (entry.compareTo(target) >= 0) {
        if (entry.compareTo(endTarget) < 0) {
          result.add(entry);
          count++;
        }

        // Have we seen enough?
        if (count >= maxMatchCount) {
          break;
        }
      }
    }

    return (String[]) result.toArray(new String[result.size()]);
  }

  /**
   * For the sake of diagnostics, return the locations that JSword will look for books.
   * @return the SWORD path
   */
  public String[] getSwordPath() {
    File[] filePath = SwordBookPath.getSwordPath();
    if (filePath.length == 0) {
      return ["No path"]; //$NON-NLS-1$
    }
    String[] path = new String[filePath.length];
    for (int i = 0; i < filePath.length; i++) {
      path[i] = filePath[i].getAbsolutePath();
    }
    return path;
  }

  /**
   * Determine whether the book can be searched, that is, whether
   * the book is indexed.
   *
   * @param book the book to check.
   * @return true if searching can be performed
   */
  private boolean isIndexedb(Book book) {
    return book != null && IndexManagerFactory.getIndexManager().isIndexed(book);
  }

  /**
   * Obtain a SAX event provider for the OSIS document representation of one or more book entries.
   *
   * @param bookInitials the book to use
   * @param reference a reference, appropriate for the book, of one or more entries
   */
  private SAXEventProvider getOSISProvider(String bookInitials, String reference, int start, int count) throws BookException, NoSuchKeyException {
    BookData data = getBookData(bookInitials, reference, start, count);
    SAXEventProvider provider = null;
    if (data != null) {
	try{
      	provider = data?.getSAXEventProvider();
	}catch (Exception e){
	e.printStackTrace();
	}
    }
    return provider;
  }
  private SAXEventProvider getOSISProvider(String bookInitials, Key key) throws BookException, NoSuchKeyException {
    BookData data = getBookData(bookInitials, key);
    SAXEventProvider provider = null;
    if (data != null) {
      try{
      provider = data?.getSAXEventProvider();
      }catch (Exception e) {
             //println "it is :"+data
    }
    }
    return provider;
  }
  public Book getBook(String bookInitials) {
    return Books.installed().getBook(bookInitials);
  }

  /**
   * Determine the size of this reference.
   *
   * @param bookInitials the book to which the reference applies.
   * @param reference the actual reference
   * @return the number of entries for this reference.
   * @throws NoSuchKeyException
   */
  public int getCardinality(String bookInitials, String reference) throws NoSuchKeyException {
	def rst=0
    try{
		//println ("reference:"+reference +" book ini:"+bookInitials)
	reference=reference?.trim()
	if (!reference)reference="John 3:15"
	if (reference.contains("KJV"))reference=reference.replace("KJV","Gen")
      if(reference?.startsWith("1 ")||reference?.startsWith("2 ")|| reference?.startsWith("3 ")) reference=reference.replace(" ","")
    Book book = BookInstaller.getInstalledBook(bookInitials);
    if (book != null) {
      Key key = book.getKey(reference);
      rst=key?.getCardinality();
    }
	}catch(Exception e){
		println (" exceptions for reference:"+reference +" book ini:"+bookInitials)
		e.printStackTrace()
	}
    return rst;
  }

  /**
   * Obtain the OSIS representation from a book for a reference, pruning a reference to a limited number of keys.
   *
   * @param bookInitials the book to use
   * @param reference a reference, appropriate for the book, for one or more keys
   */
  public String getOSISString(String bookInitials, String reference, int start, int count) throws BookException, NoSuchKeyException {
    String result = ""; //$NON-NLS-1$
    // println "getOSISString from "+bookInitials +" for "+reference
    try {
      SAXEventProvider sep = getOSISProvider(bookInitials, reference, start, count);
      if (sep != null) {
        ContentHandler ser = new SerializingContentHandler();
        sep?.provideSAXEvents(ser);
        result = ser?.toString();
      }
      return result;
    }
    catch (Exception ex) {
      //  throw new BookException(Msg.JSWORD_SAXPARSE, ex);
      // ex.printStackTrace();
    }
    return result;
  }

  private BookData getBookData(String bookstring, String reference, int start, int count) throws NoSuchKeyException {
	//println "bookstring:"+bookstring +" ref:"+reference
    String[] bookInitials = bookstring?.split(",");
    int len = bookInitials?.length;
	//println "bookinits:"+bookInitials.size()
    if (len < 1) {
      return null;
    } else {
      Book[] books = new Book[len];
      for (int i = 0; i < len; i++) {
        books[i] = BookInstaller.getInstalledBook(bookInitials[i]);
	//println "books:"+books[i] +" book init:"+bookInitials[i]
      }
      Key key 
	try{
	key= getBookKey(books[0], reference, start, count);
	}catch (Exception e){
	e.printStackTrace()
	}
	
      if (key && len > 1) {
        return new BookData(books, key, false);
      } else {
        return new BookData(books[0], key);
      }
    }
  }

   private BookData getBookData(String bookstring, Key key) throws NoSuchKeyException {
          Book book = BookInstaller.getInstalledBook(bookstring);
            return new BookData(book, key);
    }

  /**
   * Get just the canonical text of one or more book entries without any markup.
   *
   * @param bookInitials the book to use
   * @param reference a reference, appropriate for the book, of one or more entries
   */
  public String getPlainText(String bookInitials, String reference) {
    String result = ""
    try {
      Book book = getBook(bookInitials);
      if (book != null) {
        Key key = book.getKey(reference);
        BookData data = new BookData(book, key);
        result = OSISUtil.getCanonicalText(data.getOsisFragment());
      }
    }
    catch (Exception e) {
      e.printStackTrace()
    }
    return result;
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

  def listkey(String bookInitial,String query)  {
     Book book = BookInstaller.getInstalledBook(bookInitial);
   /*  Key key =book.getKey(query+"~");
        println " query "+query
       key.each {k->
       println "the key:"  +  k.getName()
     }
    println "the global"*/
     Key key=book.getGlobalKeyList()
    def list=new ArrayList()
    key?.each{k->
          if (k.getName()?.toUpperCase()?.contains(query.toUpperCase())) {
             list.add(k.getName())
         }
       }

   /*  render(contentType: "text/xml") {
       results {
        key.each {k->
          if (k.getName().toUpperCase().contains(query.toUpperCase())) {
           result {
             name(k.getName())
           }
         }
       }
     }
   }*/
    list
  }

  def getBookKey(Book book, String reference, int start, int count) throws NoSuchKeyException {
    if (book == null || reference == null || count < 1) {
      return null;
    }

    // TODO(dms): add trim(count) and trim(start, count) to the key interface.
    Key key = null;
    if (BookCategory.BIBLE.equals(book.getBookCategory())) {
      key = book?.getKey(reference);
      Passage remainder = ((Passage) key);
      if (start > 0) {
        remainder = remainder.trimVerses(start);
      }
      if (remainder != null) {
        remainder.trimVerses(count);
        key = remainder;
      }
    }
    else if (BookCategory.GENERAL_BOOK.equals(book.getBookCategory())) {
      // At this time we cannot trim a General Book
      key = book.getKey(reference);
    }
    else {
      key = book.getKey(reference);

      // Do we need to trim?
      if (start > 0 || key.getCardinality() > count) {
        Iterator iter = key.iterator();
        key = book.createEmptyKeyList();
        int i = 0;
        while (iter.hasNext()) {
          i++;
          if (i <= start) {
            // skip it
            iter.next();
            continue;
          }
          if (i >= count) {
            break;
          }
          key.addAll((Key) iter.next());
        }
      }
    }
    return key;
  }

  public List getChapters(String bible) {
    int bn = BibleInfo.getBookNumber(bible);

    if (bn < 0) {
      bn = 1;
      // System.out.println("not found book number for book:" + bible);
    }
    int cn = 0;
    try {
      cn = BibleInfo.chaptersInBook(bn);
    } catch (Exception ne) {
      ne.printStackTrace();
    }
    List reply = new ArrayList();
    try {
      for (int i = 1; i <= cn; i++) {
        reply.add(i);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return reply
  }

  /**
   * Get a listing of all the available books.
   *
   * @param filter The custom filter specification string
   * @return a list of (initial, name) string pairs
   * @see BookInstaller#getInstalledBook(String)
   */
  def getInstalledBooks(String filter) {
    // println "filter:" + filter
    List reply = new ArrayList();

    List books = BookInstaller.getInstalledBooks(filter);
    //println "num:" + books.size()
    Iterator iter = books.iterator();
    while (iter.hasNext()) {
      Book book = (Book) iter.next();

      //    println "book info:" + book.getName()+" "+book.getInitials()
      def rbook = [book.getInitials(), book.getName()]

      reply.add(rbook);
    }

    // If we can't find a book, indicate that.
    if (reply.size() == 0) {
      reply.add(["", "No Books installed"]); //$NON-NLS-1$ //$NON-NLS-2$
    }

    // return (String[][]) reply.toArray(new String[reply.size()][]);
    reply
  }

  def printVersesbybookchapter(String book,int chapter){
    int bn = BibleInfo.getBookNumber(book);
       int cn = BibleInfo.chaptersInBook(bn);
       int vn=BibleInfo.versesInChapter(bn,chapter)
    def x=new StringBuffer()
    (0..vn).each{
     x<<book+" "+chapter+":"+it+"\n"
    }
    def o=new File("c:/tmp/"+book+chapter+".txt")
    o.write(x.toString())
  }
   def printVersesBybook(String book){
     println book
    int bn = BibleInfo.getBookNumber(book);
       int cn = BibleInfo.chaptersInBook(bn);

    def x=new StringBuffer()
    (1..cn).each{chapter->
       int vn=BibleInfo.versesInChapter(bn,chapter)
      (1..vn).each{
            x<<book+" "+chapter+":"+it+"\n"
      }

    }
    def o=new File("c:/tmp/"+book+".txt")
    o.write(x.toString())
  }
  public static void main(String[] args) {
    def me = new BibleService()
 /*   def vs1 = me.randomVerse()
    println vs1
    vs1 = me.randomVerse()
    println vs1
    vs1 = me.randomVerse()
    println vs1
    vs1 = me.randomVerse()
    println vs1
    me.printVersesbybookchapter("gen",1)
    me.printVersesBybook("psa")
    me.printVersesBybook("pro")
    me.printVersesBybook("jam")
    me.printVersesBybook("1pe")
    me.printVersesBybook("2pe")
    me.printVersesBybook("1jo")
    me.printVersesBybook("2jo")
    me.printVersesBybook("3jo")
    me.printVersesBybook("jude")
    me.printVersesBybook("eph")
    me.printVersesBybook("rom")
    me.printVersesBybook("phili")
    me.printVersesBybook("col")*/
   me.listkey("easton","").each{
     println it
   }
   // me.listkey("ZhEnglish","love")
    
  }
}
