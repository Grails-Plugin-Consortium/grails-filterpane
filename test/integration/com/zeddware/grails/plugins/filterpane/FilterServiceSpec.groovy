package com.zeddware.grails.plugins.filterpane

import grails.plugin.spock.IntegrationSpec

/**
 */
class FilterServiceSpec extends IntegrationSpec {

    def filterPaneService

    def "filter books no count"() {
        given:
        def params = ['filter': [op: [title: 'ILike'], title: 'think']]
        Book.findOrSaveWhere(title: 'i like to think')
        Book.findOrSaveWhere(title: 'think about it')
        Book.findOrSaveWhere(title: 'i love turtles')

        when:
        def results = filterPaneService.filter(params, Book)

        then:
        Book.list().size() == 3
        2 == results?.size()
    }

    def "get books by id"() {
        given:
        def book = Book.findOrSaveWhere(title: 'i like to think')
        def params = ['filter': [op: [id: 'Equal'], id: book.id]]

        when:
        List<Book> books = (List<Book>)filterPaneService.filter(params, Book)

        then:
        books.size() == 1
        books[0].id == book.id
        books[0] == book
    }

    def "get author count for book by title"() {
        given:
        def params = ['filter': [op: ['books.title': 'ILike'], 'books.title': 'think']]
        Book.findOrSaveWhere(title: 'i like turtles')
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Cool', lastName: 'Dude'))
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Another', lastName: 'Dude'))

        when:
        def authorCount = filterPaneService.count(params, Author)

        then:
        authorCount == 2
    }

    def "get author for book by association filter"() {
        given:
        def params = ['filter': [op: ['author.lastName': 'Equal'], 'author.lastName': 'Dude']]
        Book.findOrSaveWhere(title: 'i like turtles')
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Cool', lastName: 'Dude'))
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Another', lastName: 'Dude'))

        when:
        List<Book> books = (List<Book>)filterPaneService.filter(params, Book)

        then:
        books.size() == 1
        books[0].authors.size() == 2
        books[0].authors.find {it.firstName == 'Cool'}
        books[0].authors.find {it.firstName == 'Another'}
    }

    def "test for empty params"() {
        given:
        def params = [:]
        5.times {
            Book.findOrSaveWhere(title: "Book ${it}")
        }

        when:
        List<Book> books = (List<Book>)filterPaneService.filter(params, Book)

        then:
        books.size() == 5
        5.times {
            books.find {it.title == "Book ${it}"}
        }
    }
}
