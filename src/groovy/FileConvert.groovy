def sourcefile="westcatech.html.org"
def source=new File(sourcefile).getText("UTF-8")
def writer=null
writer = new File(sourcefile+"_UTF8").newWriter("BIG5", true)
writer.write(source)
writer.close()
