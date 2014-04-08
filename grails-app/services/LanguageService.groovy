class LanguageService {

    boolean transactional = false

  /**
    * Cheating. Treat all CJK as Chinese. Good enough here
   */
    def isChinese(String text) {
	if(text?.size()>0)
      		text?.charAt(0)>'\u2FFF'
    }
    def public static final DEFAULT_CHINESE_BIBLE="ChiUns"
    def public static final DEFAULT_ENGLISH_BIBLE="KJV"
    def public static final DEFAULT_CHINESE_DICTIONARY="YANHUILAIWORD"
    def public static final DEFAULT_ENGLISH_DICTIONARY="Easton"

    def isChinesebook(String bookname){
      chinesebooks.contains(bookname)
    }
    def therightbible(String bookname,String key){
      if ((isChinese(key) && isChinesebook(bookname))||(!isChinese(key) && !isChinesebook(bookname))){
        return bookname
      } else if (isChinese(key)){
        return DEFAULT_CHINESE_BIBLE
      } else{
        return DEFAULT_ENGLISH_BIBLE
      }
    }
     def therightdic(String bookname,String key){
      if ((isChinese(key) && isChinesebook(bookname))||(!isChinese(key) && !isChinesebook(bookname))){
        return bookname
      } else if (isChinese(key)){
        return DEFAULT_CHINESE_DICTIONARY
      } else{
        return DEFAULT_ENGLISH_DICTIONARY
      }
    }
   def private static final chinesebooks=['lzzbible','ChiUns','ChiUn','ChiNCVs','ChiNCVt','YANHUILAI','YANHUILAIWORD','ANCIENTNAMES','YANHUILAINAMELOC','ZhCharlesHZhao','CHZTT','ZhHanzi'] as Set


  public static void main(String[] aregs){
    def x=new LanguageService()
    def c="abc"
    x.detect(c)
    c="ABC"

   
   println x.detect(c)

    char cc='\u3000' //the first japanese char
    println "the firt jap:"+ cc+"  the int:"+(int)cc
    c="\u3000\u4000"
     println x.detect(c)

  }
}
