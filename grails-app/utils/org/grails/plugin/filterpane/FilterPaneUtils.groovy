package org.grails.plugin.filterpane

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.web.converters.ConverterUtil
import java.text.SimpleDateFormat


/**
 * @author skrenek
 *
 */
class FilterPaneUtils {
	private static Logger log = Logger.getLogger(FilterPaneUtils.class)
	private static SimpleDateFormat df = new SimpleDateFormat('EEE MMM dd hh:mm:ss zzz yyyy')

	static java.util.Date parseDateFromDatePickerParams(def paramProperty, def params) {
		try {
			if (params[paramProperty] instanceof Date) {

				return (Date)params[paramProperty]

			} else if (params[paramProperty] instanceof String) {

				try {
					return df.parse(params[paramProperty])
				} catch (Exception ex) {
					/* Do nothing. */
					log.debug("Parse exception for ${params[paramProperty]}: ${ex.message}")
				}
			}

			def year = params["${paramProperty}_year"]
			def month = params["${paramProperty}_month"]
			def day = params["${paramProperty}_day"]
			def hour = params["${paramProperty}_hour"]
			def minute = params["${paramProperty}_minute"]
			boolean paramExists = (minute || hour || day || month || year)

//            if (log.isDebugEnabled()) {
//                log.debug("Parsing date from params: ${year} ${month} ${day} ${hour} ${minute}")
//            }

			String format = ''
			String value = ''
			if (year != null) {
				format = "yyyy"
				value = year
			}
			if (month != null) {
				format += 'MM'
				value += zeroPad(month)
			}
			if (day != null) {
				format += 'dd'
				value += zeroPad(day)
			}
			if (hour != null) {
				format += 'HH'
				value += zeroPad(hour)
			} else if (paramProperty.endsWith('To')) {
				format += 'HH'
				value += '23'
			}

			if (minute != null) {
				format += 'mm'
				value += zeroPad(minute)
			} else if (paramProperty.endsWith('To')) {
				format += 'mm:ss.SSS'
				value += '59:59.999'
			}

			if (value == '' || ! paramExists) { // Don't even bother parsing.  Just return null if blank.
				return null
			}

			if (log.isDebugEnabled()) log.debug("Parsing ${value} with format ${format}")
			return Date.parse(format, value)// new java.text.SimpleDateFormat(format).parse(value)
		} catch (Exception ex) {
			log.error("${ex.getClass().simpleName} parsing date for property ${paramProperty}: ${ex.message}")
			return null
		}
	}

	static Date getBeginningOfDay(def aDate) {
		if (aDate == null) return null
		if (Date.isAssignableFrom(aDate.class)) {
			Date date = (Date)aDate;
			Calendar calendar = Calendar.getInstance()
			calendar.setTime(date);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			return calendar.getTime();
		}
		return null
	}

	static Date getEndOfDay(def aDate) {
		if (aDate == null) return null
		if (Date.isAssignableFrom(aDate.class)) {
			Date date = (Date)aDate
			Calendar calendar = Calendar.getInstance()
			calendar.setTime(date);
			calendar.set(Calendar.HOUR_OF_DAY, 23);
			calendar.set(Calendar.MINUTE, 59);
			calendar.set(Calendar.SECOND, 59);
			calendar.set(Calendar.MILLISECOND, 999);
			return calendar.getTime();

		}
	}

	private static def zeroPad(def val) {
		try {
			if (val != null) {
				int i = val as int
				if (i < 10) {
					return "0${i}"
				} else {
					return val
				}
			}
		} catch (Exception ex) {
			return val
		}
	}

	static def extractFilterParams(params) {
		def ret = [:]
		params.each { entry ->
			if (entry.key.startsWith("filter.") || entry.key.equals("filterProperties") || entry.key.equals("filterBean")) {
				ret[entry.key] = entry.value
			}
		}
		return ret
	}

	static def extractFilterParams(def params, boolean datesToStruct) {
		def ret = [:]
		params.each { entry ->
			if (entry.key.startsWith("filter.") || entry.key.equals("filterProperties") || entry.key.equals("filterBean")) {
				def val = entry.value
				if (datesToStruct && val instanceof Date) val = 'struct'
				ret[entry.key] = val
			}
		}
		return ret
	}

	static boolean isFilterApplied(params) {
		boolean isApplied = false
		params.each { key, value ->
			if (key.startsWith('filter.op') && value != null && ! ''.equals(value)) {
				isApplied = true
				return
			}
		}
		return isApplied
	}

	static def resolveDomainClass(def grailsApplication, def bean) {
		if (log.isDebugEnabled()) log.debug("resolveDomainClass: bean is ${bean?.class}")
		if(bean instanceof GrailsDomainClass) {
			return bean
		}
		String beanName = null
		if (bean instanceof Class) {
			beanName = bean.name
		} else if(bean instanceof String) {
			beanName = bean
		}
		if (beanName) {
			def result = grailsApplication.getDomainClass(beanName)
			if (result == null)
				result = ConverterUtil.getDomainClass(beanName)
			return result
		}
		return null
	}

	static def resolveDomainProperty(def grailsApplication, def domainClass, def property) {

		if ("id".equals(property) || "identifier".equals(property))
			return domainClass.identifier

		def thisDomainProp = domainClass.persistentProperties.find {
			it.name == property
		}
		return thisDomainProp
	}
	
	static def getOperatorMapKey(def opType) {
		if (opType.getSimpleName().equalsIgnoreCase("boolean")) {
			return 'boolean'
		} else if (opType == Integer || opType == int || opType == Long || opType == long
			|| opType == Double || opType == double || opType == Float || opType == float
			|| opType == Short || opType == short || opType == BigDecimal || opType == BigInteger) {
			return 'numeric'
		} else if (java.util.Date.isAssignableFrom(opType)) {
			return 'date'
		} else if (opType.isEnum()) {
			return 'enum'
		} else if (opType.simpleName.equalsIgnoreCase("currency")) {
			return 'currency'
		}
		return 'text'
	}
}
