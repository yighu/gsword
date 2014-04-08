def source= "/Users/yiguanghu/ccim/phonegapprayscripturehtml/www.org/"
def dest= "/Users/yiguanghu/ccim/phonegapprayscripturehtml/www/"
def foot=new File(source+"footer.html").getText();
(0..93).each{
def sourcetxt=new File(source+"day"+it+".htm").getText("UTF-8")
def writer=null
writer = new File(dest+"day"+it+".htm").newWriter("UTF-8", true)
writer.write(sourcetxt.replace("</body>",foot+"</body>"))
writer.close()
}
