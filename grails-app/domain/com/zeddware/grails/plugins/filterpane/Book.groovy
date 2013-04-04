package com.zeddware.grails.plugins.filterpane

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class Book {

    static belongsTo = Author
    static hasMany = [authors: Author]

    String title

    static constraints = {
        authors nullable: true
    }
}
