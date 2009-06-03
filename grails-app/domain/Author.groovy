class Author {
	String firstName = ''
	String lastName = ''
	
	static hasMany = [ books: Book ]
	
	String toString() {
		return "${lastName}, ${firstName}"
	}
}
