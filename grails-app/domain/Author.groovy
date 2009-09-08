class Author {
	String firstName = ''
	String lastName = ''
        FavoriteGenre favoriteGenre
	
	static hasMany = [ books: Book ]
        Set books
	
	String toString() {
		return "${lastName}, ${firstName}"
	}
}
