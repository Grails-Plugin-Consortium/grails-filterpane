class FilterpaneGrailsPlugin {
    def version = "2.5.0"
    def grailsVersion = "2.4 > *"

    def author = "Grails Plugin Consortium"
    def authorEmail = ""
    def title = "Grails FilterPane Plugin"
    def description = "This plugin adds automatic and customizable filtering capabilities to any Grails application's list views."
    def documentation = "http://grails-plugin-consortium.github.io/grails-filterpane/"

    def developers = [
        [name: "Steve Krenek", email: "zeddmaxim@gmail.com"],
        [name: "Christian Oestreich", email: "acetrike@gmail.com"],
        [name: "Jonas Stenberg", email: "jonas.stenberg@21grams.com"]]

    def license = 'APACHE'

    def issueManagement = [system: 'JIRA', url: 'http://jira.grails.org/browse/GPFILTERPANE']
    def scm = [url: "https://github.com/Grails-Plugin-Consortium/grails-filterpane"]

    def pluginExcludes = [
        'grails-app/conf/codenarc.groovy',
        'grails-app/conf/codenarc.ruleset.all.groovy.txt',
        'grails-app/domain/**',
        'grails-app/controllers/**',
        'grails-app/views/book/**',
        'src/docs/**',
        'codenarc.properties'
    ]
}
