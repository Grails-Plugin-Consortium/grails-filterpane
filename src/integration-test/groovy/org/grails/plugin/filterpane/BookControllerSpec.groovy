package org.grails.plugin.filterpane

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.integration.Integration
import grails.util.Holders
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@Integration
@Mock([Publisher, Author, Book])
@TestFor(BookController)
class BookControllerSpec extends Specification {
    @Autowired
    FilterPaneService filterPaneService

    def setup(){
        filterPaneService = new FilterPaneService()
        filterPaneService.grailsApplication= Holders.getGrailsApplication()
        controller.filterPaneService = filterPaneService
    }

    def "test emdash filter on controller"() {
        given:
        Publisher p = new Publisher(firstName: 'Some', lastName: 'Publisher').save(validate:false)
        Author dm = new Author(firstName: 'Dave', lastName: 'Mark', favoriteGenre: FavoriteGenre.Reference, publisher: p).save(validate:false)
        new Book(authors: [dm], title: 'Hello�how are you', releaseDate: java.sql.Date.valueOf('2008-11-01'), inStock: true, price: 39.99, cost: 27.99, readPriority: 'Normal', bookType: BookType.Reference).save(validate:false)

        when:
        params.filter = [op: [title: 'ILike'], title: '�']
        controller.filter()
        def model = controller.modelAndView.model

        then:
        model.bookList.size() == 1
        model.bookCount == 1
        model.bookList.find { it.title == 'Hello�how are you'}
    }

    def "test filter by title text on controller"() {
        given:
        Publisher p = new Publisher(firstName: 'Some', lastName: 'Publisher').save(validate:false)
        Author dm = new Author(firstName: 'Dave', lastName: 'Mark', favoriteGenre: FavoriteGenre.Reference, publisher: p).save(validate:false)
        new Book(authors: [dm], title: 'I like cheese', releaseDate: java.sql.Date.valueOf('2008-11-01'), inStock: true, price: 39.99, cost: 27.99, readPriority: 'Normal', bookType: BookType.Reference).save(validate:false)
        new Book(authors: [dm], title: 'I like apples', releaseDate: java.sql.Date.valueOf('2008-11-01'), inStock: true, price: 39.99, cost: 27.99, readPriority: 'Normal', bookType: BookType.Reference).save(validate:false)

        when:
        params.filter = [op: [title: 'ILike'], title: 'like']
        controller.filter()
        def model = controller.modelAndView.model

        then:
        model.bookList.size() == 2
        model.bookCount == 2
        model.bookList.find { it.title == 'I like cheese'}
    }
}
