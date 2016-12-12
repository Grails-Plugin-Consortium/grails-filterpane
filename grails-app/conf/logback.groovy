import grails.util.BuildSettings
import grails.util.Environment


// See http://logback.qos.ch/manual/groovy.html for details on configuration
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%level %logger - %msg%n"
    }
}

logger("org.grails.plugins.filterpane", DEBUG, ["STDOUT"], false)
logger("org.hibernate.SQL", DEBUG, ["STDOUT"], false)
logger("org.hibernate.type.descriptor.sql.BasicBinder", TRACE, ["STDOUT"], false)

root(ERROR, ['STDOUT'])

if (Environment.current == Environment.DEVELOPMENT) {
    def targetDir = BuildSettings.TARGET_DIR
    if (targetDir) {

        appender("FULL_STACKTRACE", FileAppender) {

            file = "${targetDir}/stacktrace.log"
            append = true
            encoder(PatternLayoutEncoder) {
                pattern = "%level %logger - %msg%n"
            }
        }
        logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false)
    }
}
