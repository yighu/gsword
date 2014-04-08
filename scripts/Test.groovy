import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU
import java.net.URL;
 import java.util.Iterator;
 import java.util.List;

 import javax.xml.transform.TransformerException;

 import org.crosswire.common.util.NetUtil;
 import org.crosswire.common.util.ResourceUtil;
 import org.crosswire.common.xml.Converter;
 import org.crosswire.common.xml.SAXEventProvider;
 import org.crosswire.common.xml.TransformingSAXEventProvider;
 import org.crosswire.common.xml.XMLUtil;
 import org.crosswire.jsword.book.Book;
 import org.crosswire.jsword.book.BookData;
 import org.crosswire.jsword.book.BookException;
 import org.crosswire.jsword.book.BookFilter;
 import org.crosswire.jsword.book.BookFilters;
 import org.crosswire.jsword.book.BookMetaData;
 import org.crosswire.jsword.book.Books;
 import org.crosswire.jsword.book.BooksEvent;
 import org.crosswire.jsword.book.BooksListener;
 import org.crosswire.jsword.book.OSISUtil;
 import org.crosswire.jsword.index.search.DefaultSearchModifier;
 import org.crosswire.jsword.index.search.DefaultSearchRequest;
 import org.crosswire.jsword.passage.Key;
 import org.crosswire.jsword.passage.NoSuchKeyException;
 import org.crosswire.jsword.passage.Passage;
 import org.crosswire.jsword.passage.PassageTally;
 import org.crosswire.jsword.passage.RestrictionType;
 import org.crosswire.jsword.passage.Verse;
 import org.crosswire.jsword.util.ConverterFactory;
 import org.crosswire.jsword.versification.BibleInfo;
 import org.xml.sax.SAXException;

 
grailsHome = Ant.project.properties."environment.GRAILS_HOME"

includeTargets << new File ( "${grailsHome}/scripts/Init.groovy" )  

target('default': "The description of the script goes here!") {
    doStuff()
}

target(doStuff: "The implementation task") {

}

def doStruff(){




          Book bible = Books.installed().getBook("kjv")

          Key key = bible.getKey("Gen 1 1")
          BookData data = new BookData(bible, key)
          SAXEventProvider osissep = data.getSAXEventProvider()

          Converter styler = ConverterFactory.getConverter()

          TransformingSAXEventProvider htmlsep = (TransformingSAXEventProvider) styler.convert(osissep)

          // You can also pass parameters to the xslt. What you pass depends upon what the xslt can use.
          BookMetaData bmd = bible.getBookMetaData()
          boolean direction = bmd.isLeftToRight()
          htmlsep.setParameter("direction", direction ? "ltr" : "rtl")

          // Finally you can get the styled text.
          String text = XMLUtil.writeToString(htmlsep)

          println "The html text of Gen 1:1 is " + text


}