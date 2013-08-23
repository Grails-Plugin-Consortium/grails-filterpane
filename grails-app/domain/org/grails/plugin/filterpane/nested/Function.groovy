package org.grails.plugin.filterpane.nested

import groovy.transform.ToString

// Temporary fix due to groovy version
@ToString(includes='name')
class Function {

    String name = 'function'

    static belongsTo = [Part]
}
