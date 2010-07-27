package com.zeddware.grails.plugins.filterpane

import grails.test.*

class FilterUtilsTests extends GrailsUnitTestCase {

	def params
	Date testDate

    protected void setUp() {
        super.setUp()
		testDate = java.sql.Timestamp.valueOf('2010-07-26 20:38:15.000')

		params = [testDate:testDate,
			testDate_year:'2010',
			testDate_month:'7',
			testDate_day:'26',
			testDate_hour:'20',
			testDate_minute:'38',
			testDate_second:'15'
		]
    }

    protected void tearDown() {
        super.tearDown()
    }

    // parseDateFromDatePickerParams tests
	void testParseFromDate() {
		assertNotNull('value was null', FilterUtils.parseDateFromDatePickerParams('testDate', params))
	}

	void testParseFromToString() {
		params.testDate = params.testDate.toString()
		assertNotNull('value was null', FilterUtils.parseDateFromDatePickerParams('testDate', params))
	}

	void testParseFromStruct() {
		params.testDate = 'struct'
		assertNotNull('value was null', FilterUtils.parseDateFromDatePickerParams('testDate', params))
	}
}
