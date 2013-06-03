confirmAll = false
confirmNone = false
deleteAll = false

printMessage = { String message -> event('StatusUpdate', [message]) }
finished = {String message -> event('StatusFinal', [message])}
errorMessage = { String message -> event('StatusError', [message]) }

def copy = {String source, String target ->
    printMessage "Copying ${source} to ${target}"
    def overwrite = confirmAll
    if(confirmNone) overwrite = false
    def input = ""

    // only if dir already exists in, ask to overwrite it
    if(new File(target).exists()) {
        printMessage "Files arealy exist in ${target}.  If you wish to override, please copy those you wish to override manually from the plugin source."
        overwrite = false
//        if(isInteractive && !overwrite && !confirmNone){
//            println 'y = yes, n = no, a = overwrite all, s = skip all'
//            input = grailsConsole.userInput("Overwrite existing files in ${target}?", ["y", "n", "a" ,"s"] as String[])
//        }
//        if(!isInteractive || input == "y" || input == "a") overwrite = true
//        if(input == "a") confirmAll = true
//        if(input == "s") confirmNone = true
    } else {
        ant.mkdir(dir: target)
        overwrite = true    // nothing to overwrite but will be copied (state this in the event message)
    }

    if(new File(source).isDirectory()) ant.copy(todir: "$target", overwrite: overwrite) { fileset dir: "$source" }
    else ant.copy(todir: "$target", overwrite: overwrite) { fileset file: "$source" }

    finished "Filterpane Files ${overwrite ? '' : 'not '}installed."
}

copy("${pluginBasedir}/grails-app/views/filterpane", "${basedir}/grails-app/views/filterpane")