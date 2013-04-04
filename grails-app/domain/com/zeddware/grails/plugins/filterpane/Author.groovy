package com.zeddware.grails.plugins.filterpane

class Author {

    static hasMany = [books: Book]

    String firstName
    String lastName

}
