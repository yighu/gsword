import org.crosswire.jsword.book.*
import org.crosswire.jsword.versification.*
import java.util.concurrent.*

class BibleindexService {

  boolean transactional = false
  def kjvkeys
  def chiunskeys
  def chiunkeys
  def chiunkeys_size
  def kjvkeys_size
  def chiunskeys_size
  def grailsApplication
  def findindex(List keylist, String key){
	def result=0
	for (int i=0;i<keylist.size();i++){
	 if (keylist.get(i).equals(key)){
		result=i
		break
	}
	}
	result
	}
  def thekjvKeys() {
    if (kjvkeys == null) {
      
      kjvkeys = new File(grailsApplication.config.keyroot + "/KJV.key").getText().split(';') as List;
      kjvkeys_size = kjvkeys?.size()
    }
    kjvkeys
  }

  def thechiunKeys() {
    if (chiunkeys == null) {
      chiunkeys = new File(grailsApplication.config.keyroot + "/ChiUn.key").getText().split(';') as List;
      chiunkeys_size = chiunkeys?.size()
    }
    chiunkeys
  }
  def kjvkeysize(){
    if(!kjvkeys_size){
      thekjvKeys()
    }
    kjvkeys_size
  }
   def chiunkeysize(){
     if(!chiunkeys_size){
        thechiunKeys()
     }
     chiunkeys_size
  }
    def chiunskeysize(){
      if(!chiunskeys_size){
        thechiunsKeys()
     }
     chiunskeys_size
  }
  def thechiunsKeys() {
    if (chiunskeys == null) {
      chiunskeys = new File(grailsApplication.config.keyroot + "/ChiUns.key").getText().split(';') as List;
      chiunskeys_size = chiunskeys?.size()
    }
    chiunskeys
  }

  def indexerEnglish(String version) {
    def jswordserice = new JswordService()
    ConcurrentSkipListSet words = new ConcurrentSkipListSet<String>();
    int nbook = BibleInfo.booksInBible();
    def start = Calendar.getInstance().getTime()
    try {
      for (int i = 1; i <= nbook; i++) {
        def bookshortName = BibleInfo.getBookName(i).getShortName()
        def chapters = BibleInfo.chaptersInBook(i);
        //println " chapters "+chapters
        for (int chapter = 1; chapter <= chapters; chapter++) {

          def text = jswordserice.getPlainText(version, bookshortName + " " + chapter)
          //  def key = bble.getKey(bookshortName + " " + chapter)
          // def text = bble.getRawText(key)
          //  println "text:"+text
          StringTokenizer tokens = new StringTokenizer<String>(text)
          while (tokens.hasMoreTokens()) {

            def t = tokens.nextToken()?.replace(':', '').replace('?', '').replace('!', '').replace('(', '').replace(')', '').replace(',', '').replace('.', '').replace('\'s', '').replace('\'', '').trim()
            if (!t.isEmpty()) {
              words.add(t)
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace()
    }
 //   println " Total keys in " + version + " :" + words.size()
    new File("c:\\tmp\\" + version + ".key").write(words.join(';'))
    def end = Calendar.getInstance().getTime()
    println start
    println end
  }

  def indexerChinese(String version) {
    def jswordserice = new JswordService()
    ConcurrentSkipListSet words = new ConcurrentSkipListSet<String>();
    int nbook = BibleInfo.booksInBible();
    def start = Calendar.getInstance().getTime()
    try {
      for (int i = 1; i <= nbook; i++) {
        def bookshortName = BibleInfo.getBookName(i).getShortName()

        def chapters = BibleInfo.chaptersInBook(i);
        //println " chapters "+chapters
        for (int chapter = 1; chapter <= chapters; chapter++) {

          def text = jswordserice.getPlainText(version, bookshortName + " " + chapter)
          //  def key = bble.getKey(bookshortName + " " + chapter)
          // def text = bble.getRawText(key)
          //  println "text:"+text
          StringTokenizer tokens = new StringTokenizer<String>(text)
          while (tokens.hasMoreTokens()) {

            def t = tokens.nextToken()?.replace(':', '').replace('?', '').replace('!', '').replace('(', '').replace(')', '').replace(',', '').replace('.', '').replace('\'s', '').replace('\'', '').trim()
            if (!t.isEmpty()) {
              words.add(t)
            }
          }
        }

      }
    } catch (Exception e) {
      e.printStackTrace()
    }
  //  println " Total keys in " + version + " :" + words.size()
    def out = new java.io.PrintWriter(new java.io.File("c:\\tmp\\" + version + ".key"), "UTF-8");
    out.print(words.join(';'));
    out.flush();
    out.close();


    def end = Calendar.getInstance().getTime()
    println start
    println end
  }

  public static void main(String[] args) {
    def me = new BibleindexService()
    me.indexerEnglish("KJV")
    me.indexerChinese("ChiUns")
    me.indexerChinese("ChiUn")

  }
}


