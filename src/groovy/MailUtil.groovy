import org.crosswire.common.xml.*
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.mail.*;
import javax.activation.*;
import javax.mail.internet.*;
import org.crosswire.jsword.book.*
import java.net.MalformedURLException;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
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

  final String BIBLE_PROTOCOL = "bible";                                     //$NON-NLS-1$
  final String DICTIONARY_PROTOCOL = "dict";                                      //$NON-NLS-1$
  final String GREEK_DEF_PROTOCOL = "gdef";                                      //$NON-NLS-1$
  final String HEBREW_DEF_PROTOCOL = "hdef";                                      //$NON-NLS-1$
  final String GREEK_MORPH_PROTOCOL = "gmorph";                                    //$NON-NLS-1$
  final String HEBREW_MORPH_PROTOCOL = "hmorph";                                    //$NON-NLS-1$
  final String COMMENTARY_PROTOCOL = "comment";                                   //$NON-NLS-1$
  final String STRONGS_NUMBERS = "Strongs"; //$NON-NLS-1$
//binding['jswordService']=new JswordService()
binding['jswordService']=new BibleService()
  binding['xslurl'] = ResourceUtil.getResource("iBD.xsl");
  def final DELIM = ";"
  def getSchedule() {
  def dailyschedule = new HashMap()
    if (dailyschedule.isEmpty()) {
  def scheduletxt = ResourceUtil.getResource("daily.txt");
      scheduletxt.eachLine {line ->
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


  public String readStyledText(String mainbook, String reference, int start, int maxKeyCount) throws NoSuchKeyException, BookException, SAXException {
    Book book = jswordService.getBook(mainbook);
    SAXEventProvider osissep = jswordService.getOSISProvider(mainbook, reference, start, maxKeyCount);
    if (osissep == null) {
      return ""; //$NON-NLS-1$
    }

    TransformingSAXEventProvider htmlsep = new TransformingSAXEventProvider(NetUtil.toURI(xslurl), osissep); //Customize xslt

    htmlsep.setParameter("VLine", true)
    htmlsep.setParameter("Strongs", "false")
    htmlsep.setParameter("Morph", "false")
    BookMetaData bmd = book.getBookMetaData();
    boolean direction = bmd.isLeftToRight();
    htmlsep.setParameter("direction", direction ? "ltr" : "rtl"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    return XMLUtil.writeToString(htmlsep);
  }

  String day() {
    def dt = new Date();
    java.text.DateFormat dateFormat = new SimpleDateFormat("MM.dd")
    dateFormat.format(dt)
  }


  def public file2string(String inputfile,String encode){
	String tmline="",line="";

      		FileInputStream fis =new FileInputStream(inputfile);
      		InputStreamReader isr = new InputStreamReader(fis, encode);
      		BufferedReader reader = new BufferedReader(isr);
      while ((tmline = reader.readLine()) != null) {
		line+="<br>\n"+tmline.trim();	
	   }
	line
  }
  def public sendMail(from, to, subject, content,encoding)
  {
     Properties props = new Properties();
     props.put("mail.host", "rock.ccim.org");
     Session session = Session.getDefaultInstance(props, null);
     Message msg = new MimeMessage(session);
     msg.setFrom(new InternetAddress( from ));
     InternetAddress[] addressTo = new InternetAddress[1];
     addressTo[0] = new InternetAddress( to );
     msg.setRecipients(Message.RecipientType.TO, addressTo);
     msg.setHeader("Content-Encoding",encoding);
     msg.setSubject(subject);
     BodyPart messagebody=new MimeBodyPart();
	
     messagebody.setContent(content, "text/html;charset=utf-8");
     Multipart multipart=new MimeMultipart();
	multipart.addBodyPart(messagebody);
	msg.setContent(multipart);
     try
     {
        Transport.send(msg);
     }
     catch (e)
     {
         e.printStackTrace()
     }
  }
  def public sendBig5Mail(from, to, subject, content)
  {
     // init sesssion
     Properties props = new Properties();
     props.put("mail.host", "rock.ccim.org");

     Session session = Session.getDefaultInstance(props, null);

     // create a message
     Message msg = new MimeMessage(session);

     // set addresses
     msg.setFrom(new InternetAddress( from ));

     InternetAddress[] addressTo = new InternetAddress[1];
     addressTo[0] = new InternetAddress( to );
     msg.setRecipients(Message.RecipientType.TO, addressTo);
     msg.setContentLanguage(("big5"));
     msg.setHeader("Content-Encoding","big5");
     msg.setSubject(subject);
     msg.setContent(content, "text/plain");

     try
     {
        Transport.send(msg);
     }
     catch (e)
     {
         e.printStackTrace()
     }
  }
  def public sendMail(from, to, subject, content)
  {
     // init sesssion
     Properties props = new Properties();
     props.put("mail.host", "rock.ccim.org");

     Session session = Session.getDefaultInstance(props, null);

     // create a message
     Message msg = new MimeMessage(session);

     // set addresses
     msg.setFrom(new InternetAddress( from ));

     InternetAddress[] addressTo = new InternetAddress[1];
     addressTo[0] = new InternetAddress( to );
     msg.setRecipients(Message.RecipientType.TO, addressTo);

     // set the subject and content
     msg.setSubject(subject);
     msg.setContent(content, "text/plain");

     try
     {
        Transport.send(msg);
        //log.info("mail sent..")
     }
     catch (e)
     {
         e.printStackTrace()
         //log.warn("failed to send error mail: " + e)
     }
  }
  def public sendMailwAttachment(from, to, subject, content,attachedfile)
  {
     // init sesssion
     Properties props = new Properties();
     props.put("mail.host", "rock.ccim.org");

     Session session = Session.getDefaultInstance(props, null);

     // create a message
     Message msg = new MimeMessage(session);

     // set addresses
     msg.setFrom(new InternetAddress( from ));

     InternetAddress[] addressTo = new InternetAddress[1];
     addressTo[0] = new InternetAddress( to );
     msg.setRecipients(Message.RecipientType.TO, addressTo);

     // set the subject and content
     msg.setSubject(subject);

// Create the message part
       BodyPart messageBodyPart = new MimeBodyPart();

// Fill the message
       messageBodyPart.setText(content);

       Multipart multipart = new MimeMultipart();
       multipart.addBodyPart(messageBodyPart);

// Part two is attachment
       messageBodyPart = new MimeBodyPart();
       String filename = attachedfile;
       DataSource source = new FileDataSource(filename);
       messageBodyPart.setDataHandler(new DataHandler(source));
       messageBodyPart.setFileName(filename);
       multipart.addBodyPart(messageBodyPart);

 msg.setContent(multipart);
     try
     {
        Transport.send(msg);
        //log.info("mail sent..")
     }
     catch (e)
     {
         e.printStackTrace()
         //log.warn("failed to send error mail: " + e)
     }
  }

def public sendMailToMultiwAttach(from, recipients, subject, content, attachedfile) {
  // init sesssion
  Properties props = new Properties();
  props.put("mail.host", "rock.ccim.org");
  Session session = Session.getDefaultInstance(props, null);

  // create a message
  Message msg = new MimeMessage(session);

  // set addresses From
  msg.setFrom(new InternetAddress( from ));

  // set recipients
  InternetAddress[] addressTo = new InternetAddress[recipients.length];
  int count =0;
  recipients.each{
   addressTo[count++] = new InternetAddress( it );
  }
  msg.setRecipients(Message.RecipientType.TO, addressTo);

  // set the subject and content
  msg.setSubject(subject);

  // Create the message part
  BodyPart messageBodyPart = new MimeBodyPart();

  // Fill the message
  messageBodyPart.setText(content);

  Multipart multipart = new MimeMultipart();
  multipart.addBodyPart(messageBodyPart);

  // Part two is attachment
  messageBodyPart = new MimeBodyPart();
  String filename = attachedfile;
  DataSource source = new FileDataSource(filename);
  messageBodyPart.setDataHandler(new DataHandler(source));
  messageBodyPart.setFileName(filename);
  multipart.addBodyPart(messageBodyPart);

  msg.setContent(multipart);

  // send mail
  try{
     Transport.send(msg);
  } catch (e) {
      e.printStackTrace()
  }
}

  def public sendMultiMail(from, to, subject, content)
  {
     // init sesssion
     Properties props = new Properties();
     props.put("mail.host", "rock.ccim.org");

     Session session = Session.getDefaultInstance(props, null);

     // create a message
     Message msg = new MimeMessage(session);

     // set addresses
     msg.setFrom(new InternetAddress( from ));

     InternetAddress[] addressTo = new InternetAddress[to.length];
     int count =0;
     to.each{
      addressTo[count++] = new InternetAddress( it );
     }
     msg.setRecipients(Message.RecipientType.TO, addressTo);
     // set the subject and content
     msg.setSubject(subject);
     msg.setContent(content, "text/plain");

     try
     {
        Transport.send(msg);
        //log.info("mail sent..")
     }
     catch (e)
     {
         e.printStackTrace()
         //log.warn("failed to send error mail: " + e)
     }
  }

//Prayer scripture
def prayerindex(){
def file=new File("prayerindex.txt")
if(!file.isFile())file.write("0");
def index=file?.text
if(!index){
	index="1"
}
int i=index.toInteger()
if(i>92)i=0
i++
file.write(""+i);
i
}

def prayer(){
def index=prayerindex()
file="/Users/yiguanghu/ccim/prayerministry/daily/tmp/day${index}.htm"
content=file2string(file ,"UTF8")
//to="streams-in-the-desert-gb@googlegroups.com"
def subject="Pray the Scripture"
def from="mailman@ccim.org"
to="yighu@yahoo.com"
sendMail(from,to, subject, content,"UTF8")
}
def head=new File("Head.txt").text
def end=new File("endhtml.txt").text


def from="mailman@ccim.org"
def now=Calendar.getInstance().time
def format=new SimpleDateFormat("MMM-dd-yyyy")
def nw=format.format(now)

def subject="One Year Through Bible (UTF8)-${nw}" 
def content=dailytxt("ChiUn") 
	content=head+content+end
def to="yhu@goantiques.com"
to="one-year-through-bible-hb5@googlegroups.com"
  sendMail(from,to, subject, content,"UTF8")
new File("/home/bible/www/bible_b5.html").write(content)

content=dailytxt("ChiUns") 
	content=head+content+end
to="one-year-through-bible-hgb@googlegroups.com"
  sendMail(from,to, subject, content,"UTF8")
new File("/home/bible/www/bible_gb.html").write(content)

subject="One Year Through Bible (UTF8)-${nw}"
content=dailytxt("ChiNCVt") 
	content=head+content+end
to="one-year-through-bible-nb5@googlegroups.com"
  sendMail(from,to, subject, content,"UTF8")
new File("/home/bible/www/bible_nb.html").write(content)

content=dailytxt("ChiNCVs") 
	content=head+content+end
to="one-year-through-bible-ngb@googlegroups.com"
  sendMail(from,to, subject, content,"UTF8")
new File("/home/bible/www/bible_ng.html").write(content)

content=dailytxt("KJV") 
	content=head+content+end
to="one-year-through-bible-kjv@googlegroups.com"
  sendMail(from,to, subject, content,"UTF8")
new File("/home/bible/www/bible_kj.html").write(content)

content=dailytxt("BBE") 
	content=head+content+end
to="one-year-through-bible-bbe@googlegroups.com"
  sendMail(from,to, subject, content,"UTF8")
new File("/home/bible/www/bible_be.html").write(content)

subject="Streams In the Desert (UTF8)-${nw} "  
def months=[ "jan", "feb", "mar", "apr", "may", "june", "july", "aug", "sept", "oct", "nov", "dec"] as List
now=Calendar.instance
def mon=now.get(now.MONTH)
def dy=now.get(now.DAY_OF_MONTH)
def encoding="b5"
def tail=new File("/home/tomcat/dailyscript/tail.txt").text
def file="/var/www/html/streams/${encoding}/${months[mon]}/${months[mon]}-${dy}.txt";

content=file2string(file ,"big5")
to="streams-in-the-desert-b5@googlegroups.com"
 sendMail(from,to, subject, content+tail,"UTF8")

encoding="gb"
file="/var/www/html/streams/${encoding}/${months[mon]}/${months[mon]}-${dy}.txt";
content=file2string(file ,"gb2312")
to="streams-in-the-desert-gb@googlegroups.com"
 sendMail(from,to, subject, content+tail,"UTF8")

prayer()
