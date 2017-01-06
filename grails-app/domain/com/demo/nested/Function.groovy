package com.demo.nested

import groovy.transform.ToString

@ToString(includes = ['id', 'name'])
class Function {

    String name = 'function'

    static belongsTo = [Part]

    @Override
    String toString() {
        return "$id - $name"
    }
}
