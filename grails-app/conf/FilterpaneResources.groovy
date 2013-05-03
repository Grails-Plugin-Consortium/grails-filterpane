modules = {
    filterpane {
        defaultBundle 'filterpane'

        resource url: [plugin: 'filterpane', dir: 'js', file: 'fp.js']
        resource url: [plugin: 'filterpane', dir: 'css', file: 'fp.css'], disposition: 'head'
    }
}