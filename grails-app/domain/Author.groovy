class Author {
	String firstName = ''
	String lastName = ''
        FavoriteGenre favoriteGenre
	
	static hasMany = [ books: Book ]
	
	String toString() {
		return "${lastName}, ${firstName}"
	}
}
