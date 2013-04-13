package org.grails.plugin.filterpane

import grails.plugin.spock.IntegrationSpec
import grails.test.GroovyPagesTestCase
import grails.test.mixin.TestMixin

import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException
import org.springframework.web.context.request.RequestContextHolder

@TestMixin(GroovyPagesTestCase)
class FilterPaneTabLibSpec extends IntegrationSpec {

    def "test includes with no params"() {
        when:
        def output = applyTemplate('<filterpane:includes />',[:])

        then:
        output == """<link rel="stylesheet" type="text/css" href="/css/fp.css" />\n<script type="text/javascript" src="/js/fp.js"></script>"""
    }

    def "test is filtered no output"() {
        when:
        def output = applyTemplate('<filterpane:isFiltered>testing</filterpane:isFiltered>',[:])

        then:
        output == ''
    }

    def "test is filtered with output"() {
        given:
        RequestContextHolder.currentRequestAttributes().params['filter.op.test'] = 'Like'

        when:
        def output = applyTemplate('<filterpane:isFiltered>testing</filterpane:isFiltered>',[:])

        then:
        output == 'testing'
    }

    def "test is not filtered no output"() {
        given:
        RequestContextHolder.currentRequestAttributes().params['filter.op.test'] = 'Like'

        when:
        def output = applyTemplate('<filterpane:isNotFiltered>testing</filterpane:isNotFiltered>',[:])

        then:
        output == ''
    }

    def "test is not filtered with output"() {
        when:
        def output = applyTemplate('<filterpane:isNotFiltered>testing</filterpane:isNotFiltered>',[:])

        then:
        output == 'testing'
    }

    def "test filter link with no values"() {
        when:
        def output = applyTemplate('<filterpane:filterLink >testing</filterpane:filterLink>',[:])

        then:
        thrown(GrailsTagException)
    }

    def "test filter link"() {
        when:
        def output = applyTemplate('<filterpane:filterLink controller=\'book\' values=\'[author:"Mr Test"]\'>testing</filterpane:filterLink>',[:])

        then:
        output == '<a href="/book/filter?sort=&amp;order=&amp;filter.op.author=Equal&amp;filter.author=Mr+Test">testing</a>'
    }

    def "test filter link is null"() {
        when:
        def output = applyTemplate('<filterpane:filterLink controller=\'book\' values=\'[author:[op:"IsNull"]]\'>testing</filterpane:filterLink>',[:])

        then:
        output == '<a href="/book/filter?sort=&amp;order=&amp;filter.op.author=IsNull&amp;filter.author=0">testing</a>'
    }

    def "test filter link between"() {
        when:
        def output = applyTemplate('<filterpane:filterLink controller=\'book\' values=\'[dateCreated:[op:"Between", value:"2013-01-01", to:"2013-01-31" ]]\'>testing</filterpane:filterLink>',[:])

        then:
        output == '<a href="/book/filter?sort=&amp;order=&amp;filter.op.dateCreated=Between&amp;filter.dateCreated=2013-01-01&amp;filter.dateCreatedTo=2013-01-31">testing</a>'
    }
}
