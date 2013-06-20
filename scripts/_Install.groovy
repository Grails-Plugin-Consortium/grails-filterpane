confirmAll = false
confirmNone = false
deleteAll = false

printMessage = { String message -> event('StatusUpdate', [message]) }
finished = { String message -> event('StatusFinal', [message]) }
errorMessage = { String message -> event('StatusError', [message]) }

def copy = { String source, String target ->
    printMessage "Copying ${source} to ${target}"
    // only if dir already exists in, ask to overwrite it
    if(new File(target).exists()) {
        printMessage "Files arealy exist in ${target}.  If you wish to override, please copy those you wish to override manually from the plugin source."
    } else {
        ant.mkdir(dir: target)
    }

    if(new File(source).isDirectory()) {
        ant.copy(todir: "$target", overwrite: false) { fileset dir: "$source" }
    } else {
        ant.copy(todir: "$target", overwrite: false) { fileset file: "$source" }
    }

    finished "Filterpane files should now exist in your project at $target."
}

copy("${pluginBasedir}/grails-app/views/filterpane", "${basedir}/grails-app/views/filterpane")