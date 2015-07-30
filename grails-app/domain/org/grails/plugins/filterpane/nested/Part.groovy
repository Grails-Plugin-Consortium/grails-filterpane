package org.grails.plugins.filterpane.nested

class Part {

    String name = 'part'

    static hasMany = [functions: Function]
    static belongsTo = [Robot]

    static constraints = {
        functions nullable: true
    }
}
