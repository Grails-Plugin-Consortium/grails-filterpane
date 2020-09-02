package org.grails.plugins.filterpane

import com.demo.Book
import grails.core.GrailsApplication
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@Integration
@Rollback
class FilterPaneServiceEmptyCriteriaSpec extends Specification {

    @Autowired
    FilterPaneService filterPaneService
    @Autowired
    GrailsApplication grailsApplication

    def "test finding by empty filter value by criteria"() {
        setup:
        def books
        Book book0 = new Book(title: null).save(flush: true)
        Book book1 = new Book(title: "").save(flush: true)
        Book book2 = new Book(title: 'Hello').save(flush: true)

        when:
        books = Book.createCriteria().list() {
            or {
                isNull('title')
                eq('title', '')
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
        Book.findOrSaveWhere(title: '')
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
        Book.findOrSaveWhere(title: null)
        Book.findOrSaveWhere(title: 'Hello')

        when:
        def results = filterPaneService.filter(params, Book)

        then:
        Book.list().size() == 2
        1 == results?.size()
    }
}
