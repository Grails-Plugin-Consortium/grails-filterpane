package com.demo.nested

class Part {

    String name = 'part'

    static hasMany = [functions: Function]
    static belongsTo = [robot: Robot]

    static constraints = {
        functions nullable: true
    }
}
