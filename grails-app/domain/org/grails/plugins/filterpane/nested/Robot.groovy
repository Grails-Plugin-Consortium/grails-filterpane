package org.grails.plugins.filterpane.nested

class Robot {

    String name = 'Bob'

    static hasMany = [parts: Part]

    static constraints = {
        parts nullable:  true
    }
}
