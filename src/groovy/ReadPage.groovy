/**
 * Created by IntelliJ IDEA.
 * User: Yiguang
 * Date: Apr 6, 2009
 * Time: 8:02:06 PM
 * To change this template use File | Settings | File Templates.
 */




def retriveURL(String url){
  StringBuffer sb=new StringBuffer()
try {
      String encoding = "ISO-8859-1";
      URL u = new URL(url);
      URLConnection uc = u.openConnection();
      String contentType = uc.getContentType();
      int encodingStart = contentType.indexOf("charset=");
      if (encodingStart != -1) {
          encoding = contentType.substring(encodingStart+8);
      }
      InputStream inp = new BufferedInputStream(uc.getInputStream());
      Reader r = new InputStreamReader(inp, encoding);
      int c;

      while ((c = r.read()) != -1) {
        sb.append((char) c);
      }
    }
    catch (MalformedURLException ex) {
      ex.printStackTrace()
    }
catch (IOException ex) {
      System.err.println(ex);
    }
 sb.toString()
}

def findHrefs(String page){
    page.split("<a").each {
      int i=it.indexOf("</a>")
      def a=it.substring(0,i+4)
      println a
    }

}

def page =retriveURL("http://www.mentu.org/node/3");
  findHrefs(page)