package org.grails.plugins.filterpane.test

class Publisher {

    static hasMany = [ authors : Author ]

    String firstName
    String lastName

    static constraints = {
        firstName()
        lastName()
    }
}
