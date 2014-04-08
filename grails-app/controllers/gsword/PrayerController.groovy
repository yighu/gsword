package gsword
import org.codehaus.groovy.grails.commons.ConfigurationHolder
class PrayerController {

def config = ConfigurationHolder.config
def prayerindex(){
def file=new File(config.prayerroot.toString() +"/prayerindex.txt")
if(!file.isFile())file.write("0");
def index=file?.text
if(!index){
	index="1"
}
int i=index.toInteger()
if(i>92)i=0
//i++
//file.write(""+i);
i
}
def intro={
def file=config.prayerroot+"/intro.htm"
def txt=new File(file).text?.replaceAll("�","")
render (view:'index',model:[txt:txt])
}

def today={

def i=prayerindex()
def file=config.prayerroot+"/day${i}.htm"
render new File(file).text
}
    def index = {

def i=params.i
try{
if (!i?.isInteger()) i=prayerindex()
if(i.toInteger()>93||i.toInteger()<0)i=prayerindex()
}catch (Exception e){}
def file=config.prayerroot+"/day${i}.htm"
def txt=new File(file).text?.replaceAll("�","")
render (view:'index',model:[txt:txt])
 }
}
