ant.mkdir(dir: "${basedir}/grails-app/views/filterpane")
ant.copy(todir: "${basedir}/grails-app/views/filterpane") {
    fileset(dir: "${pluginBasedir}/grails-app/views/filterpane")
}