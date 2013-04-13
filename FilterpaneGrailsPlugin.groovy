class FilterpaneGrailsPlugin {
    def version = "2.1"
    def grailsVersion = "2.2 > *"

    def dependsOn = [:]

	def author = "Grails Plugin Consortium"
    def authorEmail = ""
    def title = "Dynamically filter / search domain objects."
    def description = "This plugin adds automatic filtering capabilities to any Grails application's list views."

    // URL to the plugin's documentation
    def documentation = "http://grails-plugin-consortium.github.io/grails-filterpane/"

    def developers = [
            [name: "Steve Krenek", email: "zeddmaxim@gmail.com"],
            [name: "Christian Oestreich", email: "acetrike@gmail.com"],
            [name: "stenix71", email: "@stenix71"]]

    def license = 'APACHE'

    def issueManagement = [system: 'JIRA', url: 'http://jira.grails.org/browse/GPFILTERPANE']
    def scm = [url: "https://github.com/skrenek/grails-filterpane"]

    def pluginExcludes = [
            'grails-app/conf/spring/resources.groovy',
            'grails-app/conf/codenarc.groovy',
            'grails-app/conf/codenarc.ruleset.all.groovy.txt',
            'grails-app/domain/**',
            'src/docs/**',
            'codenarc.properties'
    ]

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
