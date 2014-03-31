package org.grails.plugin.filterpane

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.compiler.GrailsClassLoader

class FilterPaneService {

    static transactional = false

    GrailsApplication grailsApplication

    def filter(params, Class filterClass) {
        doFilter(params, filterClass, false)
    }

    def count(params, Class filterClass) {
        doFilter(params, filterClass, true)
    }

    private filterParse(criteria, domainClass, params, filterParams, filterOpParams, doCount) {
        // First pull out the op map and store a list of its keys.
        def keyList = []
        keyList.addAll(filterOpParams.keySet())
        keyList = keyList.sort() // Sort them to get nested properties next to each other.

        log.debug("op Keys = ${keyList}")

        // op = map entry.  op.key = property name.  op.value = operator.
        // params[op.key] is the value
        keyList.each { String propName ->
            log.debug("\n=============================================================================.")
            log.debug("== ${propName}")

            // Skip associated property entries.  (They'll have a dot in them.)  We'll use the map instead later.
            if (!propName.contains(".")) {
                def filterOp = filterOpParams[propName]
                def rawValue = filterParams[propName]
                def rawValue2 = filterParams["${propName}To"]

                // If the filterOp is a Map, then the propName is an association (e.g. Book.author)
                if ((filterOp instanceof Map && rawValue instanceof Map)) {
                    def nextFilterParams = rawValue
                    def nextFilterOpParams = filterOp

                    if (!areAllValuesEmptyRecursively(nextFilterParams)) {
                        criteria."${propName}" {
                            // Are any of the values non-empty?
                            log.debug("== Adding association ${propName}")
                            def nextDomainProp = FilterPaneUtils.resolveDomainProperty(domainClass, propName)
                            def nextDomainClass = FilterPaneUtils.resolveReferencedDomainClass(nextDomainProp)
                            // If they want to sort by an associated property, need to do it here.
                            List sort = params.sort.toString().split('\\.')
                            //todo: while this appears to output correct criteria, the sort of child objects doesn't seem to work as intended
                            if (!doCount && params.sort && sort.size() > 1 && sort.get(sort.size() - 2) == propName) {
                                order(sort.get(sort.size() - 1), params.order ?: 'asc')
                            }
                            filterParse(criteria, nextDomainClass, params, nextFilterParams, nextFilterOpParams, doCount)
                        }
                    }
                } else {
                    def thisDomainProp = FilterPaneUtils.resolveDomainProperty(domainClass, propName)
                    def val = parseValue(thisDomainProp, rawValue, filterParams, null)
                    def val2 = parseValue(thisDomainProp, rawValue2, filterParams, "${propName}To")
                    log.debug("== propName is ${propName}, rawValue is ${rawValue}, val is ${val} of type ${val?.class} val2 is ${val2} of type ${val2?.class}")
                    addCriterion(criteria, propName, filterOp, val, val2, filterParams, thisDomainProp)
                }
            } else {
                log.debug "value used ${propName} is a dot notation should switch to a nested map like [filter: [op: ['authors': ['lastName': FilterPaneOperationType.Equal]], 'authors': ['lastName': 'Dude']]]"
            }
            log.debug("==============================================================================='\n")
        }
    }

    private Boolean areAllValuesEmptyRecursively(Map map) {
        def result = true
        map.each { k, v ->
            if (v instanceof Map) {
                result = result && areAllValuesEmptyRecursively(v)
            } else {
                log.debug "${v} is empty ${v?.toString()?.trim()?.isEmpty()}"
                result = result && v?.toString()?.trim()?.isEmpty()
            }
        }
        result
    }

    private doFilter(params, Class filterClass, Boolean doCount) {
        log.debug("filtering... params = ${params.toMapString()}")
        //def filterProperties = params?.filterProperties?.tokenize(',')
        def filterParams = params.filter ? params.filter : params
        def filterOpParams = filterParams.op
//        def associationList = []
        def domainClass = FilterPaneUtils.resolveDomainClass(grailsApplication, filterClass)

        //if (filterProperties != null) {
        if (filterOpParams?.size() > 0) {

            def c = filterClass.createCriteria()

            def criteriaClosure = {
                and {
                    filterParse(c, domainClass, params, filterParams, filterOpParams, doCount)
                }

                if (doCount) {
                    c.projections {
                        if (params?.uniqueCountColumn) {
                            countDistinct(params.uniqueCountColumn)
                        } else {
                            rowCount()
                        }
                    }
                } else {
                    if (params.offset) {
                        firstResult(params.offset.toInteger())
                    }
                    if (params.max) {
                        maxResults(params.max.toInteger())
                    }
                    if (params.fetchMode) {
                        def fetchModes
                        if (params.fetchMode instanceof Map) {
                            fetchModes = params.fetchModes
                        }

                        if (fetchModes) {
                            fetchModes.each { association, mode ->
                                c.fetchMode(association, mode)
                            }
                        }
                    }
                    def defaultSort
                    try {
                        def gdb
                        Class clz = new GrailsClassLoader().loadClass('org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainBinder')
                        if (clz) {
                            gdb = clz.newInstance()
                            if (gdb?.class?.simpleName == 'GrailsDomainBinder') {
                                defaultSort = gdb?.getMapping(filterClass)?.sort
                            }
                        }
                    } catch (Exception ex) {
                        log.info ex
                        log.info("No mapping property found on filterClass ${filterClass}")
                    }
                    if (params.sort) {
                        if (params.sort.indexOf('.') < 0) { // if not an association..
                            order(params.sort, params.order ?: 'asc')
                        }
                    } else if (defaultSort != null) {
                        log.debug('No sort specified and default is specified on domain. Using it.')
                        // Grails >2.3 uses SortConfig for default sort
                        if (defaultSort instanceof String) {
                            // backward support for older grails
                            order(defaultSort, params.order ?: 'asc')
                        } else {
                            defaultSort.namesAndDirections?.each { name, direction ->
                                order(name, direction ?: 'asc')
                            }
                        }
                    } else {
                        log.debug('No sort parameter or default sort specified.')
                    }
                }
            } // end criteria

            Closure doListOperation = { p ->
                (p?.listDistinct == true ?
                        c.listDistinct(criteriaClosure) : c.list(criteriaClosure))
            }

            def results = doCount ? c.get(criteriaClosure) : doListOperation(params)

            if (doCount && results instanceof List) {
                results = 0I
            }
            return results
        } else {
            // If no valid filters were submitting, run a count or list.  (Unfiltered data)
            if (doCount) {
                return filterClass.count()//0I
            }
            return filterClass.list(params)
        }
    }

    private addCriterion(criteria, propertyName, op, value, value2, filterParams, domainProperty) {
        if(!op){
            log.debug('Skipping due to null operation')
            return;
        }
        log.debug("Adding ${propertyName} ${op} ${value} value2 ${value2}")
//        boolean added = true

        // GRAILSPLUGINS-1320.  If value is instance of Date and op is Equal and
        // precision on date picker was 'day', turn this into a between from
        // midnight to 1 ms before midnight of the next day.
        boolean isDayPrecision = "y".equals(filterParams["${domainProperty?.domainClass?.name}.${domainProperty?.name}_isDayPrecision"])
        boolean isOpAlterable = (op == FilterPaneOperationType.Equal || op == FilterPaneOperationType.NotEqual)
        if (value != null && isDayPrecision && Date.isAssignableFrom(value.class) && isOpAlterable) {
            op = (op == FilterPaneOperationType.Equal) ? 'Between' : 'NotBetween'
            value = FilterPaneUtils.getBeginningOfDay(value)
            value2 = FilterPaneUtils.getEndOfDay(value)
            log.debug("Date criterion is Equal to day precision.  Changing it to between ${value} and ${value2}")
        }

        def criteriaMap = [(FilterPaneOperationType.Equal.operation): 'eq', (FilterPaneOperationType.NotEqual.operation): 'ne',
                (FilterPaneOperationType.LessThan.operation): 'lt', (FilterPaneOperationType.LessThanEquals.operation): 'le',
                (FilterPaneOperationType.GreaterThan.operation): 'gt', (FilterPaneOperationType.GreaterThanEquals.operation): 'ge',
                (FilterPaneOperationType.Like.operation): 'like', (FilterPaneOperationType.ILike.operation): 'ilike',
                (FilterPaneOperationType.IBeginsWith.operation): 'ilike', (FilterPaneOperationType.BeginsWith.operation): 'like',
                (FilterPaneOperationType.IEndsWith.operation): 'ilike', (FilterPaneOperationType.EndsWith.operation): 'like']

        //needs null check since '' or 0 are valid filter
        if(op && value != null) {
            switch (op) {
                case FilterPaneOperationType.Equal.operation:
                case FilterPaneOperationType.NotEqual.operation:
                case FilterPaneOperationType.LessThan.operation:
                case FilterPaneOperationType.LessThanEquals.operation:
                case FilterPaneOperationType.GreaterThan.operation:
                case FilterPaneOperationType.GreaterThanEquals.operation:
                    criteria."${criteriaMap.get(op)}"(propertyName, value)
                    break
                case FilterPaneOperationType.Like.operation:
                case FilterPaneOperationType.ILike.operation:
                    if (!value.startsWith('*')) {
                        value = "*${value}"
                    }
                    if (!value.endsWith('*')) {
                        value = "${value}*"
                    }
                    criteria."${criteriaMap.get(op)}"(propertyName, value?.replaceAll("\\*", "%"))
                    break
                case FilterPaneOperationType.BeginsWith.operation:
                case FilterPaneOperationType.IBeginsWith.operation:
                    if (!value.endsWith('*')) {
                        value = "${value}*"
                    }
                    criteria."${criteriaMap.get(op)}"(propertyName, value?.replaceAll("\\*", "%"))
                    break
                case FilterPaneOperationType.EndsWith.operation:
                case FilterPaneOperationType.IEndsWith.operation:
                    if (!value.startsWith('*')) {
                        value = "*${value}"
                    }
                    criteria."${criteriaMap.get(op)}"(propertyName, value?.replaceAll("\\*", "%"))
                    break
                case 'NotLike':
                    if (!value.startsWith('*')) {
                        value = "*${value}"
                    }
                    if (!value.endsWith('*')) {
                        value = "${value}*"
                    }
                    criteria.not {
                        criteria.like(propertyName, value?.replaceAll("\\*", "%"))
                    }
                    break
                case 'NotILike':
                    if (!value.startsWith('*')) {
                        value = "*${value}"
                    }
                    if (!value.endsWith('*')) {
                        value = "${value}*"
                    }
                    criteria.not {
                        criteria.ilike(propertyName, value?.replaceAll("\\*", "%"))
                    }
                    break
                case 'IsNull':
                    criteria.isNull(propertyName)
                    break
                case 'IsNotNull':
                    criteria.isNotNull(propertyName)
                    break
                case 'Between':
                    criteria.between(propertyName, value, value2)
                    break
                case 'NotBetween':
                    criteria.not { between(propertyName, value, value2) }
                    break
                case 'InList':
                    criteria.inList(propertyName, value)
                    break
                case 'NotInList':
                    criteria.not { inList(propertyName, value) }
                    break
                default:
                    break
            } // end op switch
        }
    }

    /**
     * Parse the user input value to the domain property type.
     * @returns The input parsed to the appropriate type if possible, else null.
     */
    def parseValue(domainProperty, val, params, associatedPropertyParamName) {
        def newValue = val
        if (newValue instanceof String) {
            newValue = newValue.trim() ?: ''
        }

        // GRAILSPLUGINS-1717.  Groovy truth treats empty strings as false.  Compare against null.
        if (newValue != null) {
            Class cls = domainProperty?.referencedPropertyType ?: domainProperty.type
            String clsName = cls.simpleName.toLowerCase()
            log.debug("cls is enum? ${cls.isEnum()}, domainProperty is ${domainProperty}, type is ${domainProperty.type}, refPropType is ${domainProperty.referencedPropertyType} value is '${newValue}', clsName is ${clsName}")

            if ("class".equals(clsName)) {
                def tempVal = newValue
                newValue = null // default to null.  If it's valid, it'll get replaced with the real value.

                // the default class property (automatically added by GORM if class has some sub classes) needs
                // to be put as String into criteria however custom Class property needs to be type of Class
                def resolveClassValue
                if (domainProperty?.name == "class") {
                    resolveClassValue = { classValue -> classValue.toString() }
                } else {
                    resolveClassValue = { classValue ->
                        try {
                            return Class.forName(classValue.toString(), false, Thread.currentThread().contextClassLoader)
                        }
                        catch (Exception e) {
                            log.error("Cannot resolve class $classValue for filter.", e)
                            return null
                        }
                    }
                }
                // resolve value
                if (tempVal instanceof Object[]) {
                    newValue = tempVal.collect { resolveClassValue(it.toString()) }
                } else if (tempVal.toString().length() > 0) {
                    newValue = resolveClassValue(tempVal.toString())
                }
            } else if (domainProperty.isEnum()) {
                def tempVal = newValue
                newValue = null // default to null.  If it's valid, it'll get replaced with the real value.
                try {
                    if (tempVal instanceof Object[]) {
                        newValue = tempVal.collect { Enum.valueOf(cls, it.toString()) }
                    } else if (tempVal.toString().length() > 0) {
                        newValue = Enum.valueOf(cls, tempVal.toString())
                    }
                } catch (IllegalArgumentException iae) {
                    log.debug("Enum valueOf failed. value is ${tempVal}", iae)
                    // Ignore this.  val is not a valid enum value (probably an empty string).
                }
            } else if ("boolean".equals(clsName)) {
                newValue = newValue.toBoolean()
            } else if ("byte".equals(clsName)) {
                try {
                    newValue = new Byte(newValue) // no isByte()
                } catch (NumberFormatException e) {
                    newValue = null
                    log.debug e
                }
            } else if ("int".equals(clsName) || "integer".equals(clsName)) {
                newValue = newValue.isInteger() ? newValue.toInteger() : null
            } else if ("long".equals(clsName)) {
                try {
                    newValue = newValue.toLong()
                } //no isShort()
                catch (NumberFormatException e) {
                    newValue = null
                    log.debug e
                }
            } else if ("double".equals(clsName)) {
                newValue = newValue.isDouble() ? newValue.toDouble() : null
            } else if ("float".equals(clsName)) {
                newValue = newValue.isFloat() ? newValue.toFloat() : null
            } else if ("short".equals(clsName)) {
                try {
                    newValue = newValue.toShort()
                } //no isShort()
                catch (NumberFormatException e) {
                    newValue = null
                    log.debug e
                }
            } else if ("bigdecimal".equals(clsName)) {
                newValue = newValue.isBigDecimal() ? newValue.toBigDecimal() : null
            } else if ("biginteger".equals(clsName)) {
                newValue = newValue.isBigInteger() ? newValue.toBigInteger() : null
            } else if (FilterPaneUtils.isDateType(cls)) {
                def paramName = associatedPropertyParamName ?: domainProperty.name
                newValue = FilterPaneUtils.parseDateFromDatePickerParams(paramName, params, cls)
            } else if ("currency".equals(clsName)) {
                try {
                    newValue = Currency.getInstance(newValue.toString())
                } catch (IllegalArgumentException iae) {
                    // Do nothing.
                    log.debug iae
                }
            }
        }
        newValue
    }
}
