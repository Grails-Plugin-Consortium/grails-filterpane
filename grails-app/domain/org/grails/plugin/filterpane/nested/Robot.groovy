package org.grails.plugin.filterpane.nested

class Robot {

    String name = 'Bob'

    static hasMany = [parts: Part]

    static constraints = {
        parts nullable:  true
    }
}
