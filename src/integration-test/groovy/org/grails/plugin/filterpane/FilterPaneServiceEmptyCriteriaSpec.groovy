package org.grails.plugin.filterpane

import grails.core.GrailsApplication
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@Integration
@Rollback
class FilterPaneServiceEmptyCriteriaSpec extends Specification {

    @Autowired
    FilterPaneService filterPaneService
    @Autowired
    GrailsApplication grailsApplication

    def setup() {
        filterPaneService = new FilterPaneService()
        filterPaneService.grailsApplication = grailsApplication
    }

    def "test finding by empty filter value by criteria"() {
        setup:
        def books
        Book book0, book1, book2
        Book.withNewSession {
            book0 = new Book(title: null).save(flush: true)
            book1 = new Book(title: '').save(flush: true)
            book2 = new Book(title: 'Hello').save(flush: true)
            book1.title = ''
            book1.save(flush: true)
        }

        when:
        Book.withNewSession {
            books = Book.createCriteria().list() {
                or {
                    isNull('title')
                    eq('title', '')
                }
            }
        }

        then:
        book0.title == null
        book1.title == ''
        book2.title != ''
        Book.list().size() == 3
        2 == books?.size()
    }

    def "test finding by empty filter value"() {
        given:
        def params = ['filter': [op: [title: 'Equal'], title: '']]
        def book = Book.findOrSaveWhere(title: '')
        book.title = '' //this isn't being set up findorsave
        book.save(flush: true)
        Book.findOrSaveWhere(title: 'Hello')

        when:
        def results = filterPaneService.filter(params, Book)

        then:
        Book.list().size() == 2
        1 == results?.size()
    }

    def "test finding by empty filter value null value in params"() {
        given:
        def params = ['filter': [op: [title: 'IsNull'], title: '']]
        Book.findOrSaveWhere(title: '')
        Book.findOrSaveWhere(title: 'Hello')

        when:
        def results = filterPaneService.filter(params, Book)

        then:
        Book.list().size() == 2
        1 == results?.size()
    }
}
