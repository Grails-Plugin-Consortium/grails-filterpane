package org.grails.plugins.filterpane.test.nested

class Part {

    String name = 'part'

    static hasMany = [functions: Function]
    static belongsTo = [Robot]

    static constraints = {
        functions nullable: true
    }
}
