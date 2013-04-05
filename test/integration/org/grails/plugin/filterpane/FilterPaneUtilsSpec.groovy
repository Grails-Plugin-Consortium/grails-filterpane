package org.grails.plugin.filterpane

import grails.plugin.spock.IntegrationSpec

/**
 */
class FilterPaneUtilsSpec extends IntegrationSpec {

    def "parse date from datepicker from date"() {
        given:
        def testDate = java.sql.Timestamp.valueOf('2010-07-26 20:38:15.000')

        def params = [testDate: testDate,
                testDate_year: '2010',
                testDate_month: '7',
                testDate_day: '26',
                testDate_hour: '20',
                testDate_minute: '38',
                testDate_second: '15'
        ]

        when:
        def date = FilterPaneUtils.parseDateFromDatePickerParams('testDate', params)

        then:
        date
    }

    def "parse date from datepicker from string"() {
        given:
        def testDate = java.sql.Timestamp.valueOf('2010-07-26 20:38:15.000')

        def params = [testDate: testDate.toString(),
                testDate_year: '2010',
                testDate_month: '7',
                testDate_day: '26',
                testDate_hour: '20',
                testDate_minute: '38',
                testDate_second: '15'
        ]

        when:
        def date = FilterPaneUtils.parseDateFromDatePickerParams('testDate', params)

        then:
        date
    }

    def "parse date from datepicker from struct"() {
        given:
        def params = [testDate: 'struct',
                testDate_year: '2010',
                testDate_month: '7',
                testDate_day: '26',
                testDate_hour: '20',
                testDate_minute: '38',
                testDate_second: '15'
        ]

        when:
        def date = FilterPaneUtils.parseDateFromDatePickerParams('testDate', params)

        then:
        date
    }

    def "get beginning of date"() {
        given:
        Date date = new Date()

        when:
        def beginningOfDay = FilterPaneUtils.getBeginningOfDay(date)

        then:
        beginningOfDay.hours == 0
        beginningOfDay.minutes == 0
        beginningOfDay.seconds == 0
        beginningOfDay.month == date.month
        beginningOfDay.day == date.day
        beginningOfDay.year == date.year
    }

    def "get beginning of date for null value"() {
        when:
        def beginningOfDay = FilterPaneUtils.getBeginningOfDay(null)

        then:
        !beginningOfDay
    }

    def "get end of date"() {
        given:
        Date date = new Date()

        when:
        def endOfDay = FilterPaneUtils.getEndOfDay(date)

        then:
        endOfDay.hours == 23
        endOfDay.minutes == 59
        endOfDay.seconds == 59
        endOfDay.month == date.month
        endOfDay.day == date.day
        endOfDay.year == date.year
    }

    def "get end of date for null value"() {
        when:
        def endOfDay = FilterPaneUtils.getEndOfDay(null)

        then:
        !endOfDay
    }

}
