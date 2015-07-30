package org.grails.plugins.filterpane

class Publisher {

    static hasMany = [ authors : Author ]

    String firstName
    String lastName

    static constraints = {
        firstName()
        lastName()
    }
}
