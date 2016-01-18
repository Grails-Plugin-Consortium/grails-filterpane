package org.grails.plugins.filterpane.test.nested

class Robot {

    String name = 'Bob'

    static hasMany = [parts: Part]

    static constraints = {
        parts nullable:  true
    }
}
