// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
                      xml: ['text/xml', 'application/xml'],
                      text: 'text-plain',
                      js: 'text/javascript',
                      rss: 'application/rss+xml',
                      atom: 'application/atom+xml',
                      css: 'text/css',
                      csv: 'text/csv',
                      all: '*/*',
                      json: ['application/json','text/json'],
                      form: 'application/x-www-form-urlencoded',
                      multipartForm: 'multipart/form-data'
                    ]
// The default codec used to encode data with ${}
grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"
grails.converters.encoding="UTF-8"

// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
prayerroot="/home/tomcat/prayer"
transdoc="/home/tomcat/apache-tomcat-7.0.0/webapps/gsword/transdoc"
// set per-environment serverURL stem for creating absolute links
environments {
    production {
        grails.serverURL = "http://www.changeme.com"
        docroot="/home/tomcat/apache-tomcat-7.0.0/webapps/gsword"
        membibleid="/home/tomcat/membible/lastid.txt"
        membiblesch="/home/tomcat/membible/membiblesch.txt"
      keyroot="/home/tomcat/apache-tomcat-7.0.0/webapps/gsword/keywords"
	twitter_gsword_pwd="ph0t0nfu"
	twitter_membible_pwd="ph0t0nfu"
    }
    test{
      grails.serverURL = "http://www.changeme.com"
      docroot="/home/tomcat/jetty-6.1.14/webapps/docs"

    }
    development{
      docroot="c:/tmp"
      membibleid="c:/tmp/lastid.txt"
      membiblesch="c:/tmp/membiblesch.txt"
     // docroot="/home/tomcat/apache-tomcat-6.0.20/webapps/gsword"
      keyroot="/Users/yiguanghu/keys"
        membibleid="/Users/yiguanghu/ccim/gsword/gsword/web-app/memb/lastid.txt"
        membiblesch="/Users/yiguanghu/ccim/gsword/gsword/web-app/memb/membiblesch.txt"
        prayerroot="/Users/yiguanghu/ccim/prayerministry/daily/tmp"
        transdoc="/Users/yiguanghu/ccim/prayerministry/daily/tmp"

	twitter_gsword_pwd="ph0t0nfu"
	twitter_membible_pwd="ph0t0nfu"
    }
}


// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

    error  'org.codehaus.groovy.grails.web.servlet',        // controllers
           'org.codehaus.groovy.grails.web.pages',          // GSP
           'org.codehaus.groovy.grails.web.sitemesh',       // layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping',        // URL mapping
           'org.codehaus.groovy.grails.commons',            // core / classloading
           'org.codehaus.groovy.grails.plugins',            // plugins
           'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'
}
/*
navigation.read = [controller:'bible',title:'Read Bible',action:'read',id:'read']
navigation.search = [controller:'bible',title:'Search Bible',action:'search',id:'search']
navigation.research = [controller:'gbook',title:'ReSearch Bible',action:'v',id:'research']
navigation.reclassics = [controller:'gbook',title:'Read Classics',action:'c']
navigation.recommentary = [controller:'bible',title:'Read Commentary',action:'cmnt']
navigation.dictionary = [controller:'gbook',title:'Lookup Dictionary/Topics',action:'searchdics']
navigation.dailyreading= [controller:'gbook',title:'Daily Reading',action:'oneyearbible']
*/

//grails.mail.host = "rock.ccim.org"
grails.mail.host = "localhost.localdomain"
grails.mail.port = 25
grails.mail.props = ["mail.smtp.socketFactory.port": "25",
        "mail.smtp.socketFactory.fallback": "false"]
mailman="yiguang.hu@gmail.com"

//log4j.logger.org.springframework.security='off,stdout'

// Uncomment and edit the following lines to start using Grails encoding & escaping improvements

/* remove this line 
// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside null
                scriptlet = 'none' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        filteringCodecForContentType {
            //'text/html' = 'html'
        }
    }
}
remove this line */
