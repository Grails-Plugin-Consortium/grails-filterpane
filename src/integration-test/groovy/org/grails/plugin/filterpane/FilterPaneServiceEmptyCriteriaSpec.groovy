package org.grails.plugin.filterpane

import grails.test.mixin.Mock
import grails.test.mixin.integration.Integration
import grails.util.Holders
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@Mock([Book])
@Integration
class FilterPaneServiceEmptyCriteriaSpec extends Specification {

    @Autowired
    FilterPaneService filterPaneService

    def setup(){
        filterPaneService = new FilterPaneService()
        filterPaneService.grailsApplication= Holders.getGrailsApplication()
    }

    def "test finding by empty filter value by criteria"() {
        given:
        def c = Book.createCriteria()
        def book0 = Book.findOrSaveWhere(title: null)
        def book1 = Book.findOrSaveWhere(title: '')
        def book2 = Book.findOrSaveWhere(title: 'Hello')
        book1.title = ''
        book1.save(flush: true)

        when:
        def books = c.list() {
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
