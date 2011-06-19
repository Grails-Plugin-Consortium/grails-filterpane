class FilterpaneGrailsPlugin {
    def version = "2.0.1.1"
    def grailsVersion = "1.3.3 > *"
    def dependsOn = [:]
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

	def author = "Steve Krenek"
    def authorEmail = "zeddmaxim@gmail.com"
    def title = "Dynamically filter / search domain objects."
    def description = "This plugin adds automatic filtering capabilities to any Grails application's list views."

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/filterpane"

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before 
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
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
