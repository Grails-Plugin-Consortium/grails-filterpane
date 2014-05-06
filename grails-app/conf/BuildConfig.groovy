grails.project.work.dir = 'target'

grails.project.dependency.resolution = {

    inherits 'global'
    log 'warn'
    legacyResolve true

    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenLocal()
        mavenCentral()
        mavenRepo "http://repo.grails.org/grails/libs-releases/"
        mavenRepo "http://m2repo.spockframework.org/ext/"
        mavenRepo "http://m2repo.spockframework.org/snapshots/"
    }

    dependencies {

    }

    plugins {
        compile(":joda-time:1.4")

        build(':release:3.0.1',
              ':rest-client-builder:2.0.1'){
            export = false
        }

        test ":hibernate:3.6.10.13", {
            export = false
        }

        test(":code-coverage:1.2.6",
             ":codenarc:0.20",
             ":build-test-data:2.1.2") {
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
