package org.grails.plugins.filterpane

import grails.core.GrailsApplication
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.grails.datastore.mapping.model.MappingFactory
import org.grails.datastore.mapping.model.PersistentEntity
import org.joda.time.*
import org.joda.time.base.AbstractInstant
import org.joda.time.base.AbstractPartial

import java.text.SimpleDateFormat

class FilterPaneUtils {

    private static String[] df = ["yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", 'EEE MMM dd HH:mm:ss zzz yyyy']
    private static final Log log = LogFactory.getLog(this)

    static Date parseDateFromDatePickerParams(paramProperty, params) {
        try {
            if (params[paramProperty] instanceof Date) {
                return (Date) params[paramProperty]
            }

            if (params[paramProperty] instanceof String) {
                for (int i = 0; i < df.length; i++) {
                    try {
                        return new SimpleDateFormat(df[i]).parse(params[paramProperty]?.toString())
                    } catch (Exception ex) {
                        /* Do nothing. */
                        log.debug("Parse exception for ${paramProperty} : ${params[paramProperty]}: ${ex.message} with Format ${df[i]}")
                    }
                }
            }

            def year = params["${paramProperty}_year"]
            def month = params["${paramProperty}_month"]
            def day = params["${paramProperty}_day"]
            def hour = params["${paramProperty}_hour"]
            def minute = params["${paramProperty}_minute"]
            def second = params["${paramProperty}_second"]
            boolean paramExists = (minute || hour || day || month || year || second)

//                log.debug("Parsing date from params: ${year} ${month} ${day} ${hour} ${minute}")

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
                format += 'mm'
                value += '59'
            }

            if (second != null) {
                format += 'ss'
                value += zeroPad(second)
            } else if (paramProperty.endsWith('To')) {
                format += 'ss.SS'
                value += '59.999'
            }

            if (value == '' || !paramExists) { // Don't even bother parsing.  Just return null if blank.
                return null
            }

            log.debug("Parsing ${value} with format ${format}")
            return Date.parse(format, value)// new java.text.SimpleDateFormat(format).parse(value)
        } catch (Exception ex) {
            log.error("${ex.getClass().simpleName} parsing date for property ${paramProperty}: ${ex.message}")
            return null
        }
    }

    static Object parseDateFromDatePickerParams(paramProperty, params, clazz) {
        if (Date.isAssignableFrom(clazz))
            return parseDateFromDatePickerParams(paramProperty, params)
        try {
            try {
                // dynamically call the constructor of appropriate Joda class and try to parse the time/date
                return clazz.getDeclaredConstructor(Object).newInstance(params[paramProperty])
            }
            catch (Exception ex) {
                log.debug("Parse exception for ${params[paramProperty]}: ${ex.message}")
            }

            def dateTimeRepresent = null

            def year = params["${paramProperty}_year"]
            def month = params["${paramProperty}_month"]
            def day = params["${paramProperty}_day"]
            def hour = params["${paramProperty}_hour"]
            def minute = params["${paramProperty}_minute"]
            def second = params["${paramProperty}_second"]

            if (minute || hour || day || month || year || second) {
                // certain joda class
                if (clazz == DateTime) {
                    dateTimeRepresent = new DateTime().withMillisOfSecond(0) // current date time
                    dateTimeRepresent = setDate(dateTimeRepresent, 'withYear', year)
                    dateTimeRepresent = setDate(dateTimeRepresent, 'withMonthOfYear', month)
                    dateTimeRepresent = setDate(dateTimeRepresent, 'withDayOfMonth', day)
                    dateTimeRepresent = setDate(dateTimeRepresent, 'withHourOfDay', hour)
                    dateTimeRepresent = setDate(dateTimeRepresent, 'withMinuteOfHour', minute)
                    dateTimeRepresent = setDate(dateTimeRepresent, 'withSecondOfMinute', second)
                } else if (clazz == Instant) {
                    dateTimeRepresent = new LocalDateTime().withMillisOfSecond(0)
                    // current local date time - easier implementation with LocalDateTime
                    dateTimeRepresent = setDate(dateTimeRepresent, 'withYear', year)
                    dateTimeRepresent = setDate(dateTimeRepresent, 'withMonthOfYear', month)
                    dateTimeRepresent = setDate(dateTimeRepresent, 'withDayOfMonth', day)
                    dateTimeRepresent = setDate(dateTimeRepresent, 'withHourOfDay', hour)
                    dateTimeRepresent = setDate(dateTimeRepresent, 'withMinuteOfHour', minute)
                    dateTimeRepresent = setDate(dateTimeRepresent, 'withSecondOfMinute', second)
                    dateTimeRepresent = new Instant(dateTimeRepresent.localMillis)
                } else if (clazz == LocalDate) {
                    dateTimeRepresent = new LocalDate()// current time
                    dateTimeRepresent = setDate(dateTimeRepresent, 'withYear', year)
                    dateTimeRepresent = setDate(dateTimeRepresent, 'withMonthOfYear', month)
                    dateTimeRepresent = setDate(dateTimeRepresent, 'withDayOfMonth', day)
                } else if (clazz == LocalTime) {
                    dateTimeRepresent = new LocalTime().withMillisOfSecond(0) // current local time
                    dateTimeRepresent = setDate(dateTimeRepresent, 'withHourOfDay', hour)
                    dateTimeRepresent = setDate(dateTimeRepresent, 'withMinuteOfHour', minute)
                    dateTimeRepresent = setDate(dateTimeRepresent, 'withSecondOfMinute', second)
                } else if (clazz == LocalDateTime) {
                    dateTimeRepresent = new LocalDateTime().withMillisOfSecond(0) // current local date time
                    dateTimeRepresent = setDate(dateTimeRepresent, 'withYear', year)
                    dateTimeRepresent = setDate(dateTimeRepresent, 'withMonthOfYear', month)
                    dateTimeRepresent = setDate(dateTimeRepresent, 'withDayOfMonth', day)
                    dateTimeRepresent = setDate(dateTimeRepresent, 'withHourOfDay', hour)
                    dateTimeRepresent = setDate(dateTimeRepresent, 'withMinuteOfHour', minute)
                    dateTimeRepresent = setDate(dateTimeRepresent, 'withSecondOfMinute', second)
                }

                log.debug("Joda time object created $dateTimeRepresent")
                return dateTimeRepresent
            }
            return null
        } catch (Exception ex) {
            log.error("Cannot parse date for property $paramProperty", ex)
            return null
        }
    }

    static private setDate(dateTimeRepresent, method, val) {
        def newDate = dateTimeRepresent
        if (val != null) {
            try {
                newDate = dateTimeRepresent."$method"(Integer.parseInt(val))
            } catch (Exception ex) {
                log.error "Value for $method call appears to invalid Integer.  Value is: $val", ex
            }
        }
        newDate
    }

    static Date getBeginningOfDay(aDate) {
        Date beginningOfDay = null
        if (aDate && Date.isAssignableFrom(aDate.class)) {
            Date date = (Date) aDate
            Calendar calendar = Calendar.instance.with {
                time = date
                set(HOUR_OF_DAY, 0)
                set(MINUTE, 0)
                set(SECOND, 0)
                set(MILLISECOND, 0)
                it
            }
            beginningOfDay = calendar.time
        }
        beginningOfDay
    }

    static Date getEndOfDay(aDate) {
        Date endOfDay = null
        if (aDate && Date.isAssignableFrom(aDate.class)) {
            Date date = (Date) aDate
            Calendar calendar = Calendar.instance.with {
                time = date
                set(HOUR_OF_DAY, 23)
                set(MINUTE, 59)
                set(SECOND, 59)
                set(MILLISECOND, 999)
                it
            }
            endOfDay = calendar.time
        }
        endOfDay
    }

    private static zeroPad(val) {
        try {
            if (val != null) {
                int i = val as int
                return (i < 10) ? "0${i}" : val
            }
        } catch (Exception ex) {
            log.error ex
            return val
        }
    }

    static extractFilterParams(params) {
        def ret = [:]
        params.each { entry ->
            if (entry?.key?.startsWith("filter.") || entry?.key?.equals("filterProperties") || entry?.key?.equals("filterBean")) {
                ret[entry.key] = entry.value
            }
        }
        ret
    }

    static extractFilterParams(params, boolean datesToStruct) {
        def ret = [:]
        params.each { entry ->
            if (entry.key.startsWith("filter.")
                    || entry.key.equals("filterProperties")
                    || entry.key.equals("filterBean")
                    || entry.key.equals("listDistinct")
                    || entry.key.equals("uniqueCountColumn")
            ) {
                def val = entry.value
                if (datesToStruct && val instanceof Date) {
                    val = 'struct'
                }
                ret[entry.key] = val
            }
        }
        ret
    }

    static boolean isFilterApplied(params) {
        boolean isApplied = false
        params.each { key, value ->
            if (key.startsWith('filter.op.') && value != null && !''.equals(value)) {
                isApplied = true
                return
            }
        }
        isApplied
    }

    static resolveDomainClass(GrailsApplication grailsApplication, bean) {
        String beanName
        def result = null

        log.debug("resolveDomainClass: bean is ${bean?.class}")
        if (bean instanceof PersistentEntity) {
            return bean
        }

        if (bean instanceof Class) {
            beanName = bean.name
        } else if (bean instanceof String) {
            beanName = bean
        }

        if (beanName) {
            result = grailsApplication.mappingContext.getPersistentEntity(beanName)
            if (!result) {
                def className = grailsApplication.domainClasses.find { it.clazz.simpleName == beanName }?.clazz?.name
                result = grailsApplication.mappingContext.getPersistentEntity(className)
            }
        }
        result
    }

    static resolveDomainProperty(domainClass, property) {

        if ("id".equals(property) || "identifier".equals(property)) {
            return domainClass.identity
        }

        if ("class".equals(property)) {
            return [name: "class", type: Class, domainClass: domainClass, naturalName: "Class"]
            // fake GrailsDomainClassProperty object
        }

        def thisDomainProp = domainClass.persistentProperties.find {
            it.name == property
        }

        // it could be also subclass persistent property
        if (!thisDomainProp) {
            def subClassPersistentProps = resolveSubDomainsProperties(domainClass)
            thisDomainProp = subClassPersistentProps.find { it.name == property }
        }

        thisDomainProp
    }

    static resolveReferencedDomainClass(property) {
        if (property instanceof MappingFactory) {
            return (property as MappingFactory).type
        }
        if (property?.isBasic()) {
            return property.getComponentType()
        } else {
            return property?.getAssociatedEntity()
        }
    }

    /**
     * List all persistent properties of all subclasses of given domain class. Doesn't cover properties of the domain
     * super class.
     */
    static resolveSubDomainsProperties(domainClass) {
        log.debug("resolveSubDomainProperties($domainClass)")
        def subClassPersistentProps = []
        domainClass.mappingContext.getChildEntities(domainClass).each { subDomain->
            def newProps = subDomain.getPersistentProperties().findAll {
                !subClassPersistentProps.contains(it) && !domainClass.persistentProperties.contains(it)
            }

            subClassPersistentProps.addAll(newProps)
        }

        return subClassPersistentProps
    }

    static getOperatorMapKey(opType) {
        def type = 'text'
        if (opType.getSimpleName().equalsIgnoreCase("boolean")) {
            type = 'boolean'
        } else if (opType == Byte || opType == byte || opType == Integer || opType == int || opType == Long || opType == long
                || opType == Double || opType == double || opType == Float || opType == float
                || opType == Short || opType == short || opType == BigDecimal || opType == BigInteger) {
            type = 'numeric'
        } else if (Date.isAssignableFrom(opType) || AbstractInstant.isAssignableFrom(opType) || AbstractPartial.isAssignableFrom(opType)) {
            type = 'date'
        } else if (opType.isEnum()) {
            type = 'enum'
        } else if (opType.simpleName.equalsIgnoreCase("currency")) {
            type = 'currency'
        } else if (opType == Class)
            type = 'class'
        type
    }

    static isDateType(clazz) {
        // java.util.Date, Joda Time
        return Date.isAssignableFrom(clazz) || AbstractPartial.isAssignableFrom(clazz) || AbstractInstant.isAssignableFrom(clazz)
    }
}
