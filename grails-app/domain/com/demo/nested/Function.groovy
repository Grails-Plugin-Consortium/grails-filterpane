package com.demo.nested

import groovy.transform.ToString

@ToString(includes=['name'])
class Function {

    String name = 'function'

    static belongsTo = [part: Part]
}
