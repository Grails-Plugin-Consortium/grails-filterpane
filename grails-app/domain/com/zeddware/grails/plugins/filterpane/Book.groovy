package com.zeddware.grails.plugins.filterpane
class Book {
    Author author
    Author coAuthor
    String title
    BookType bookType
    Date releaseDate
    Boolean inStock
    BigDecimal price
    Date lastUpdated
    String readPriority
    BigDecimal cost
	
    static constraints = {
        title(blank:false)
        author()
        coAuthor(nullable:true)
        bookType(nullable:true)
        releaseDate()
        price()
        inStock()
        lastUpdated(nullable:true)
        readPriority(inList:['Low','Normal','High'])
        cost(min:0.00)
    }
	
    static mapping = {
        author lazy:false
		sort title:'asc'
    }
	
    String toString() {
        return title
    }
}
