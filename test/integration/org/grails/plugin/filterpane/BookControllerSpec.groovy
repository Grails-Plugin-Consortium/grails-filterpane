package org.grails.plugin.filterpane

import grails.plugin.spock.IntegrationSpec

class BookControllerSpec extends IntegrationSpec {

    def "test emdash filter on controller"() {
        given:
        BookController bookController = new BookController()
        Publisher p = new Publisher(firstName: 'Some', lastName: 'Publisher').save()
        Author dm = new Author(firstName: 'Dave', lastName: 'Mark', favoriteGenre: FavoriteGenre.Reference, publisher: p).save()
        new Book(authors: [dm], title: 'Hello�how are you', releaseDate: java.sql.Date.valueOf('2008-11-01'), inStock: true, price: 39.99, cost: 27.99, readPriority: 'Normal', bookType: BookType.Reference).save()

        when:
        bookController.params.filter = [op: [title: 'ILike'], title: '�']
        bookController.filter()
        def model = bookController.modelAndView.model

        then:
        model.bookList.size() == 1
        model.bookCount == 1
        model.bookList.find { it.title == 'Hello�how are you'}
    }

    def "test filter by title text on controller"() {
        given:
        BookController bookController = new BookController()
        Publisher p = new Publisher(firstName: 'Some', lastName: 'Publisher').save()
        Author dm = new Author(firstName: 'Dave', lastName: 'Mark', favoriteGenre: FavoriteGenre.Reference, publisher: p).save()
        new Book(authors: [dm], title: 'I like cheese', releaseDate: java.sql.Date.valueOf('2008-11-01'), inStock: true, price: 39.99, cost: 27.99, readPriority: 'Normal', bookType: BookType.Reference).save()
        new Book(authors: [dm], title: 'I like apples', releaseDate: java.sql.Date.valueOf('2008-11-01'), inStock: true, price: 39.99, cost: 27.99, readPriority: 'Normal', bookType: BookType.Reference).save()

        when:
        bookController.params.filter = [op: [title: 'ILike'], title: 'like']
        bookController.filter()
        def model = bookController.modelAndView.model

        then:
        model.bookList.size() == 2
        model.bookCount == 2
        model.bookList.find { it.title == 'I like cheese'}
    }
}
