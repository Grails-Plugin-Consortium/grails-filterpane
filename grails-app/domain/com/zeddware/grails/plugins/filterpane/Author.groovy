package com.zeddware.grails.plugins.filterpane
class Author {
	String firstName = ''
	String lastName = ''
	FavoriteGenre favoriteGenre
	Date birthdate
	
	static hasMany = [ books: Book ]
        Set books

	static constraints = {
		firstName()
		lastName()
		birthdate(nullable:true)
		favoriteGenre()
	}
	
	String toString() {
		return "${lastName}, ${firstName}"
	}
}
