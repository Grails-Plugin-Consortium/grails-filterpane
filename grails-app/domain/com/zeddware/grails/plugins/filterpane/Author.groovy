package com.zeddware.grails.plugins.filterpane

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class Author {

    static hasMany = [books: Book]

    String firstName
    String lastName

}
