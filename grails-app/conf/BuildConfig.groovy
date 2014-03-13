grails.project.work.dir = 'target'

grails.project.dependency.resolution = {

    inherits 'global'
    log 'warn'
    legacyResolve true

    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        test ("org.spockframework:spock-grails-support:0.7-groovy-2.0" ) {
            export = false
        }
    }

    plugins {
        compile(":joda-time:1.4")

        build(':release:2.2.1',
              ':rest-client-builder:1.0.3'){
            export = false
        }

        test ":hibernate:$grailsVersion", {
            export = false
        }

        test(":code-coverage:1.2.6",
             ":codenarc:0.20",
             ":build-test-data:2.0.5") {
            export = false
        }

        test(":spock:0.7") {
            exclude "spock-grails-support"
            export = false
        }
    }
}

coverage {
    xml = true
    exclusions = ['**/*Tests*']
}

codenarc {
    processTestUnit = false
    processTestIntegration = false
    processServices = false
    processDomain = false
    propertiesFile = 'codenarc.properties'
    ruleSetFiles = 'file:grails-app/conf/codenarc.groovy'
    reports = {
        CxfClientReport('xml') {                    // The report name 'MyXmlReport' is user-defined; Report type is 'xml'
            outputFile = 'target/codenarc.xml'      // Set the 'outputFile' property of the (XML) Report
            title = 'Grails FilterPane Plugin'             // Set the 'title' property of the (XML) Report
        }
    }
}
