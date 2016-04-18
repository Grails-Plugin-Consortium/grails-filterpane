package com.demo

class Author {
    String firstName = 'first'
    String lastName = 'last'
    FavoriteGenre favoriteGenre
    Publisher publisher
    int age = -1
    Date birthdate = new Date()

    static hasMany = [ books: Book ]
    static belongsTo = [Book]

    String toString() {
        "${lastName}, ${firstName}"
    }

    static constraints = {
        firstName blank: false
        lastName blank: false
        favoriteGenre nullable: true
        publisher nullable: true
        age nullable: true
        birthdate nullable: true
    }
}
