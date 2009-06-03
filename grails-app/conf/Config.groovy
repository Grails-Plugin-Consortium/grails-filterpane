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

// set per-environment serverURL stem for creating absolute links
environments {
    production {
        grails.serverURL = "http://www.changeme.com"
    }
}

// log4j configuration
log4j = {
    appenders {
        console name:'stdout', layout:pattern(conversionPattern: '[%r] %c %m%n')

    }

    //appender.errors = "org.apache.log4j.FileAppender"
    //appender.'errors.layout'="org.apache.log4j.PatternLayout"
    //appender.'errors.layout.ConversionPattern'='[%r] %c{2} %m%n'
    //appender.'errors.File'="stacktrace.log"
    root {
        warn 'stdout'
        additivity=true
    }

    debug   "com.zeddware.grails.plugins.filterpane",
            "grails.app.tagLib.com.zeddware",
            "grails.app.service.com.zeddware",
            "grails.app.controller"

    error   "grails",
            "org.codehaus.groovy.grails",
            "org.hibernate",
            "org.springframework",
            "net.sf"
    
    //additivity.StackTrace=false
}


