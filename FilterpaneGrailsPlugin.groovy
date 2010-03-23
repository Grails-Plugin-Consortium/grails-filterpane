class FilterpaneGrailsPlugin {
    def version = '0.6.8'
    def grailsVersion = "1.1.1 > *"
    def dependsOn = [:]

    def author = "Steve Krenek"
    def authorEmail = "zeddmaxim@gmail.com"
    def title = "Dynamically filter / search domain objects."
    def description = '''\
This plugin adds automatic filtering capabilities to any Grails application's list views.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/filterpane"

    def pluginExcludes = [
        'grails-app/domain/**/*.groovy',
        'grails-app/i18n/messages-filterpane_es.properties',
        'grails-app/i18n/messages.properties',
        'grails-app/controllers/**/*.groovy',
        'grails-app/views/**/*.gsp',
        'web-app/css/tree/**/*',
        'web-app/css/main.css',
        'web-app/images/**/*',
        'web-app/js/prototype/**/*',
        'web-app/js/application.js',
        'web-app/**/*.gsp',
        'docs/**/*'
    ]

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }
   
    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)		
    }

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional)
    }
	                                      
    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }
	
    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }
}
