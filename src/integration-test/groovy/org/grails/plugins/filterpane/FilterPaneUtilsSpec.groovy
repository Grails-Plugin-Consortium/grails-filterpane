package org.grails.plugins.filterpane


import grails.web.servlet.mvc.GrailsParameterMap
import org.joda.time.*
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.Timestamp
import java.text.SimpleDateFormat

class FilterPaneUtilsSpec extends Specification {

    @Unroll
    def "parse date from only the date param #theDate #type"() {
        given:
        GrailsParameterMap params = new GrailsParameterMap(['testDate': theDate], null)

        when:
        def date = FilterPaneUtils.parseDateFromDatePickerParams('testDate', params)

        then:
        date
        date.toGMTString() == expecetedDate.toGMTString()

        where:
        //this is a bit wonky due to timezone offsets on local v. test machines
        theDate                                      | expecetedDate                                                                              | type
        Timestamp.valueOf('2005-03-26 20:38:15.000') | new Date(105, 2, 26, 20, 38, 15)                                                           | 'timestamp'
        'Sat Mar 26 21:38:15 CDT 2005'               | new SimpleDateFormat('EEE MMM dd HH:mm:ss zzz yyyy').parse('Sat Mar 26 21:38:15 CDT 2005') | 'string'

    }

    def "parse date from datepicker from date part params"() {
        given:
        Timestamp testDate = Timestamp.valueOf('2010-07-26 20:38:15.000')

        GrailsParameterMap params = new GrailsParameterMap([
                testDate_year  : '2010',
                testDate_month : '7',
                testDate_day   : '26',
                testDate_hour  : '20',
                testDate_minute: '38',
                testDate_second: '15'
        ], null)

        when:
        def date = FilterPaneUtils.parseDateFromDatePickerParams('testDate', params)

        then:
        date
        date.time == testDate.time
    }

    def "parse date from datepicker from string"() {
        given:
        Timestamp testDate = Timestamp.valueOf('2010-07-26 20:38:15.000')

        GrailsParameterMap params = new GrailsParameterMap([
                testDate_year  : '2010',
                testDate_month : '7',
                testDate_day   : '26',
                testDate_hour  : '20',
                testDate_minute: '38',
                testDate_second: '15'
        ], null)

        when:
        def date = FilterPaneUtils.parseDateFromDatePickerParams('testDate', params)

        then:
        date
        date.time == testDate.time
    }

    def "parse date from datepicker from struct"() {
        given:
        GrailsParameterMap params = new GrailsParameterMap([testDate_year  : '2010',
                                                            testDate_month : '7',
                                                            testDate_day   : '26',
                                                            testDate_hour  : '20',
                                                            testDate_minute: '38',
                                                            testDate_second: '15'
        ], null)

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

    @Unroll
    def "test isDateType for type #dateType equals #isDate"() {
        when:
        Boolean isDateType = FilterPaneUtils.isDateType(dateType)

        then:
        isDateType == isDate

        where:
        dateType      | isDate
        Date          | true
        DateTime      | true
        DateMidnight  | true
        LocalDate     | true
        LocalTime     | true
        LocalDateTime | true
        Instant       | true
        Duration      | false
        Interval      | false
        String        | false
        Integer       | false
    }


    @Unroll
    def "parse joda dates from datepicker for #theDate is #expectedDate"() {
        given:
        GrailsParameterMap params = new GrailsParameterMap([testDate_year  : '2005',
                                                            testDate_month : '3',
                                                            testDate_day   : '26',
                                                            testDate_hour  : '0',
                                                            testDate_minute: '0',
                                                            testDate_second: '0'
        ], null)

        when:
        def date = FilterPaneUtils.parseDateFromDatePickerParams('testDate', params, theDate)

        then:
        date
        date.toDate() == expectedDate

        where:
        theDate   | expectedDate
        DateTime  | new Date(105, 2, 26)
        LocalDate | new Date(105, 2, 26)
    }

    @Unroll
    def "parse joda times from datepicker for #theDate is #expectedDate"() {
        given:
        GrailsParameterMap params = new GrailsParameterMap([testDate_year  : '2005',
                                                            testDate_month : '3',
                                                            testDate_day   : '26',
                                                            testDate_hour  : '0',
                                                            testDate_minute: '0',
                                                            testDate_second: '0'
        ], null)

        when:
        def date = FilterPaneUtils.parseDateFromDatePickerParams('testDate', params, theDate)

        then:
        date
        date.toString() == expectedDate

        where:
        theDate   | expectedDate
        Instant   | '2005-03-26T00:00:00.000Z'
        LocalTime | '00:00:00.000'
    }
}
