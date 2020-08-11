package org.grails.plugins.filterpane

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import grails.testing.web.taglib.TagLibUnitTest
import org.grails.taglib.GrailsTagException
import org.springframework.web.context.request.RequestContextHolder
import spock.lang.Specification

@Integration
@Rollback
class FilterPaneTabLibSpec extends Specification implements TagLibUnitTest<FilterPaneTagLib> {

    def "test includes with no params"() {
        when:
        def output = applyTemplate('<filterpane:includes />', [:])

        then:
        output == """<asset:stylesheet src="fp.css"/>\n<asset:javascript src="fp.js"/>"""
    }

    def "test is filtered no output"() {
        when:
        def output = applyTemplate('<filterpane:isFiltered>testing</filterpane:isFiltered>', [:])

        then:
        output == ''
    }

    def "test is filtered with output"() {
        given:
        RequestContextHolder.currentRequestAttributes().params['filter.op.test'] = 'Like'

        when:
        def output = applyTemplate('<filterpane:isFiltered>testing</filterpane:isFiltered>', [:])

        then:
        output == 'testing'
    }

    def "test is not filtered no output"() {
        given:
        RequestContextHolder.currentRequestAttributes().params['filter.op.test'] = 'Like'

        when:
        def output = applyTemplate('<filterpane:isNotFiltered>testing</filterpane:isNotFiltered>', [:])

        then:
        output == ''
    }

    def "test is not filtered with output"() {
        when:
        def output = applyTemplate('<filterpane:isNotFiltered>testing</filterpane:isNotFiltered>', [:])

        then:
        output == 'testing'
    }

    def "test filter link with no values"() {
        when:
        def output = applyTemplate('<filterpane:filterLink >testing</filterpane:filterLink>', [:])

        then:
        thrown(GrailsTagException)
    }

    def "test filter link"() {
        when:
        def output = applyTemplate('<filterpane:filterLink controller=\'book\' values=\'[author:"Mr Test"]\'>testing</filterpane:filterLink>', [:])

        then:
        output == '<a href="/book/filter?sort=&amp;order=&amp;filter.op.author=Equal&amp;filter.author=Mr+Test">testing</a>'
    }

    def "test filter link is null"() {
        when:
        def output = applyTemplate('<filterpane:filterLink controller=\'book\' values=\'[author:[op:"IsNull"]]\'>testing</filterpane:filterLink>', [:])

        then:
        output == '<a href="/book/filter?sort=&amp;order=&amp;filter.op.author=IsNull&amp;filter.author=0">testing</a>'
    }

    def "test filter link between"() {
        when:
        def output = applyTemplate('<filterpane:filterLink controller=\'book\' values=\'[dateCreated:[op:"Between", value:"2013-01-01", to:"2013-01-31" ]]\'>testing</filterpane:filterLink>', [:])

        then:
        output == '<a href="/book/filter?sort=&amp;order=&amp;filter.op.dateCreated=Between&amp;filter.dateCreated=2013-01-01&amp;filter.dateCreatedTo=2013-01-31">testing</a>'
    }

    def "test filter link inlist"() {
        when:
        def output = applyTemplate('<filterpane:filterLink controller=\'book\' values=\'[bookType:[op:"InList", value:"[F, NF]" ]]\'>testing</filterpane:filterLink>', [:])

        then:
        output == '<a href="/book/filter?sort=&amp;order=&amp;filter.op.bookType=InList&amp;filter.bookType=%5BF%2C+NF%5D">testing</a>'
    }

    def "test filter link notinlist"() {
        when:
        def output = applyTemplate('<filterpane:filterLink controller=\'book\' values=\'[bookType:[op:"NotInList", value:"[R]" ]]\'>testing</filterpane:filterLink>', [:])

        then:
        output == '<a href="/book/filter?sort=&amp;order=&amp;filter.op.bookType=NotInList&amp;filter.bookType=%5BR%5D">testing</a>'
    }

    def "test filter link BeginsWith"() {
        when:
        def output = applyTemplate('<filterpane:filterLink controller=\'book\' values=\'[title:[op:"BeginsWith", value:"[R]" ]]\'>testing</filterpane:filterLink>', [:])

        then:
        output == '<a href="/book/filter?sort=&amp;order=&amp;filter.op.title=BeginsWith&amp;filter.title=%5BR%5D">testing</a>'
    }

    def "test filter link IBeginsWith"() {
        when:
        def output = applyTemplate('<filterpane:filterLink controller=\'book\' values=\'[title:[op:"IBeginsWith", value:"[R]" ]]\'>testing</filterpane:filterLink>', [:])

        then:
        output == '<a href="/book/filter?sort=&amp;order=&amp;filter.op.title=IBeginsWith&amp;filter.title=%5BR%5D">testing</a>'
    }

    def "test filter link EndsWith"() {
        when:
        def output = applyTemplate('<filterpane:filterLink controller=\'book\' values=\'[title:[op:"EndsWith", value:"[R]" ]]\'>testing</filterpane:filterLink>', [:])

        then:
        output == '<a href="/book/filter?sort=&amp;order=&amp;filter.op.title=EndsWith&amp;filter.title=%5BR%5D">testing</a>'
    }

    def "test filter link IEndsWith"() {
        when:
        def output = applyTemplate('<filterpane:filterLink controller=\'book\' values=\'[title:[op:"IEndsWith", value:"[R]" ]]\'>testing</filterpane:filterLink>', [:])

        then:
        output == '<a href="/book/filter?sort=&amp;order=&amp;filter.op.title=IEndsWith&amp;filter.title=%5BR%5D">testing</a>'
    }

    def "test boolean value checks"() {

        given:
        FilterPaneTagLib fptl = new FilterPaneTagLib()

        when:
        def result = fptl.resolveBoolAttrValue(val)

        then:
        result == expects

        where:
        val    | expects
        'y'    | true
        'yes'  | true
        'true' | true
        't'    | true
        null   | false
        ''     | false
    }

    def "test filter link unsupported operation exception"() {
        when:
        applyTemplate('<filterpane:filterLink controller=\'book\' values=\'[author:[op:"IsAny"]]\'>testing</filterpane:filterLink>', [:])

        then:
        thrown(RuntimeException)
    }
}
