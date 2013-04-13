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
}
