package org.grails.plugins.filterpane.test.nested

import groovy.transform.ToString

@ToString(includes=['name'])
class Function {

    String name = 'function'

    static belongsTo = [Part]
}
