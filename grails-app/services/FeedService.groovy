import com.sun.syndication.fetcher.*
import com.sun.syndication.fetcher.impl.*
import com.sun.syndication.feed.synd.SyndFeed

class FeedService {

    def public static final GSWORDFEED="http://twitter.com/statuses/user_timeline/33532940.rss"
    def public static final MEMBIBLE="http://twitter.com/statuses/user_timeline/80463491.rss"
    boolean transactional = false
    def public static membiblenow
    def public static gswordnow
    def jswordService
    def getMembiblenow(Integer id){
     /*
      if (membiblenow){
        return membiblenow
      } else{
       */
        membiblenow=membiblefeed(id)
        return membiblenow
     // }
    }
    def getGswordnow(){
     if(gswordnow)        {
       return gswordnow
     }        else{
       gswordnow=gswordfeed()
       return gswordnow
     }
    }
    def readFeed( url )
  {


    FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
    FeedFetcher feedFetcher = new HttpURLFeedFetcher(feedInfoCache);
    SyndFeed feed = feedFetcher.retrieveFeed(new URL(url));
    def gswords=    feed.entries[0..1].title
    
  }
  def gswordfeed(){
    gswordnow=readFeed(GSWORDFEED)
  //  gswordnow=jswordService.memVersecurrentplusprv("kjv")
  }
  def membiblefeed(Integer index){
   // membiblenow=readFeed(MEMBIBLE)
   // println membiblenow
    membiblenow=jswordService.memVersecurrentplusprv("kjv",index)

  }
}
