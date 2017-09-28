import grails.plugins.Plugin

class FilterpaneGrailsPlugin extends Plugin {
    def grailsVersion = "3.0.2 > *"

    def author = "Grails Plugin Consortium"
    def authorEmail = ""
    def title = "Grails FilterPane Plugin"
    def description = "This plugin adds automatic and customizable filtering capabilities to any Grails application's list views."
    def documentation = "http://grails-plugin-consortium.github.io/grails-filterpane/"

    def developers = [
        [name: "Steve Krenek", email: "zeddmaxim@gmail.com"],
        [name: "Christian Oestreich", email: "acetrike@gmail.com"],
        [name: "Jonas Stenberg", email: "jonas.stenberg@21grams.com"],
        [name: "Mansi Arora", email: "mansi.arora@tothenew.com"]]

    def license = 'APACHE'

    def issueManagement = [system: 'github', url: 'https://github.com/Grails-Plugin-Consortium/grails-filterpane/issues']
    def scm = [url: "https://github.com/Grails-Plugin-Consortium/grails-filterpane"]

    def pluginExcludes = [
        'grails-app/conf/codenarc.groovy',
        'grails-app/conf/codenarc.ruleset.all.groovy.txt',
        'grails-app/domain/**',
        'grails-app/controllers/**',
        'grails-app/views/book/**',
        'src/docs/**',
        'codenarc.properties',
        'grails-app/views/error.gsp',
        'grails-app/views/index.gsp',
        'grails-app/views/notFound.gsp',
        'grails-app/views/layouts/**',
        'org/grails/plugins/filterpane/test/**',
        '**/com/demo/**'
    ]
}
