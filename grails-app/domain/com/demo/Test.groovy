package com.demo

class Test {
    String title
    Date releaseDate
    Boolean inStock
    BigDecimal price
    BigDecimal pricePlusTax
    Currency currency = Currency.getInstance("USD")
    Date lastUpdated
    String readPriority
    BigDecimal cost

    static constraints = {
        title(blank:false)
        releaseDate()
        price()
        pricePlusTax()
        currency()
        inStock()
        lastUpdated(nullable:true)
        readPriority(inList:['Low','Normal','High'])
        cost(min:0.00)
    }

    static mapping = {
        pricePlusTax formula: 'price * 1.055'
        sort "title"
    }

    String toString() {
        title
    }
}
