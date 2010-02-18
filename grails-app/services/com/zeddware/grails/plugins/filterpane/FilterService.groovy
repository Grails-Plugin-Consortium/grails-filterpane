package com.zeddware.grails.plugins.filterpane

class FilterService {

    boolean transactional = true
    def grailsApplication
    
    def filter(def params, Class filterClass) {
        return filter(params, filterClass, false)
    }
    
    def count(def params, Class filterClass) {
        return filter(params, filterClass, true)
    }

    private def filter(def params, Class filterClass, boolean doCount) {
        if (log.isDebugEnabled()) log.debug("filtering... params = ${params.toMapString()}")
    	//def filterProperties = params?.filterProperties?.tokenize(',')
        def filterParams = params.filter ? params.filter : params
        def filterOpParams = filterParams.op
        def associationList = []
        def domainClass = FilterUtils.resolveDomainClass(grailsApplication, filterClass)

        //if (filterProperties != null) {
		if (filterOpParams != null && filterOpParams.size() > 0) {

            def c = filterClass.createCriteria()

            def criteriaClosure = {
                def mc = filterClass.getMetaClass()
                and {
                    // First pull out the op map and store a list of its keys.
                    def keyList = []
                    keyList.addAll(filterOpParams.keySet())
                    keyList = keyList.sort() // Sort them to get nested properties next to each other.

                    if (log.isDebugEnabled()) log.debug("op Keys = ${keyList}")
    				
                    // op = map entry.  op.key = property name.  op.value = operator.
                    // params[op.key] is the value
                    keyList.each() { propName ->
                    	if (log.isDebugEnabled()) log.debug("\n=============================================================================.")
                    	if (log.isDebugEnabled()) log.debug("== ${propName}")

                        // Skip associated property entries.  (They'll have a dot in them.)  We'll use the map instead later.
                        if (! propName.contains(".")) {
                            
                            def filterOp = filterOpParams[propName]
                            def rawValue = filterParams[propName]
                            def rawValue2 = filterParams["${propName}To"]

                            // If the filterOp is a Map, then the propName is an association (e.g. Book.author)
                            if (filterOp instanceof Map && rawValue instanceof Map) {

                                // Are any of the values non-empty?
                                if (filterOp.values().find {log.debug it; it.length() > 0} != null) {

                                    if (log.isDebugEnabled()) log.debug("== Adding association ${propName}")

                                    c."${propName}"() {

                                        filterOp.each() { opEntry ->
											def associatedDomainProp = FilterUtils.resolveDomainProperty(grailsApplication, domainClass, propName)
                                            def associatedDomainClass = associatedDomainProp.referencedDomainClass
                                            def realPropName = opEntry.key
                                            def realOp = opEntry.value
                                            def realRawValue = rawValue[realPropName]
                                            def realRawValue2 = rawValue2 != null ? rawValue2["${realPropName}To"] : null
											def parsingName = "${propName}.${realPropName}"
                                            def thisDomainProp = FilterUtils.resolveDomainProperty(grailsApplication, associatedDomainClass, realPropName)
//                                            log.debug("real prop name is ${realPropName}")
                                            def val  = this.parseValue(thisDomainProp, realRawValue, filterParams, parsingName)
                                            def val2 = this.parseValue(thisDomainProp, realRawValue2, filterParams, parsingName)
//                                            log.debug("val is ${val} and val2 is ${val2}")

                                            this.addCriterion(c, realPropName, realOp, val, val2, filterParams, thisDomainProp)
                                        }
                                        if (!doCount && params.sort && params.sort.startsWith("${propName}.")) {
                                            def parts = params.sort.split("\\.")
                                            if (parts.size() == 2) {
                                                associationList << propName
                                                order(parts[1], params.order ?: 'asc')
                                            }
                                        }
                                    } // end c.propName closure.
                                } // end if any values not empty.
                            } else {
                                log.debug("propName is ${propName}")
                                def thisDomainProp = FilterUtils.resolveDomainProperty(grailsApplication, domainClass, propName)
                                def val  = this.parseValue(thisDomainProp, rawValue, filterParams, null)
                                def val2 = this.parseValue(thisDomainProp, rawValue2, filterParams, "${propName}To")
                                if (log.isDebugEnabled()) log.debug("== propName is ${propName}, rawValue is ${rawValue}, val is ${val} of type ${val?.class} val2 is ${val2} of type ${val2?.class}")
                                this.addCriterion(c, propName, filterOp, val, val2, filterParams, thisDomainProp)
                            }
                        }
                    	if (log.isDebugEnabled()) log.debug("==============================================================================='\n")
                    } // end each op
                } // end and
                
                if (doCount) {
                    c.projections {
                        rowCount()
                    }
                } else {
                    if (params.offset) {
                        firstResult(params.offset.toInteger())
                    }
                    if (params.max) {
                        maxResults(params.max.toInteger())
                    }
                    if (params.sort) {
                        if (params.sort.indexOf('.') < 0) { // if not an association..
                            order(params.sort, params.order ?: 'asc' )
                        } else {
                            def parts = params.sort.split("\\.")
                            if (!associationList.contains(parts[0])) {
                                c."${parts[0]}" {
                                    order(parts[1], params.order ?: 'asc')
                                }
                            }
                        }
//                    } else if (filterClass.hasProperty('mapping') && filterClass.mapping.hasProperty('mapping') && filterClass.mapping.mapping.hasProperty('sort')) {
//						if (log.debugEnabled) log.debug('No sort specified and default is specified on domain.  Using it.')
//						order(domainClass.mapping.sort, params.order ?: 'asc')
					} else {
						if (log.debugEnabled) log.debug('No sort parameter or default sort specified.')
					}
                }
            } // end criteria
            def results = null
            if (doCount) {
                results = c.get(criteriaClosure)
            } else {
                results = c.list(criteriaClosure)
              
            }
            if (doCount && results instanceof List) {
                //println "Returning count of 0"
                results = 0I
            }
            return results
    	} else {
            if (doCount) {
                return filterClass.count()//0I
            }
            return filterClass.list(params)
    	}
    }
    
    private def addCriterion(def criteria, def propertyName, def op, def value, def value2, def filterParams, def domainProperty) {
    	if (log.isDebugEnabled()) log.debug("Adding ${propertyName} ${op} ${value} value2 ${value2}")
		boolean added = true

		// GRAILSPLUGINS-1320.  If value is instance of Date and op is Equal and
		// precision on date picker was 'day', turn this into a between from
		// midnight to 1 ms before midnight of the next day.
		boolean isDayPrecision = "y".equals(filterParams["${domainProperty.domainClass.name}.${domainProperty.name}_isDayPrecision"])
		boolean isOpAlterable  = (op == 'Equal' || op == 'NotEqual')
		if (value != null && isDayPrecision == true && Date.isAssignableFrom(value.class) && isOpAlterable) {
			op = (op == 'Equal') ? 'Between' : 'NotBetween'
			value = FilterUtils.getBeginningOfDay(value)
			value2 = FilterUtils.getEndOfDay(value)
			if (log.isDebugEnabled())
				log.debug("Date criterion is Equal to day precision.  Changing it to between ${value} and ${value2}")
		}

		if (value) {
			switch(op) {
				case 'Equal':
				criteria.eq(propertyName, value)
				break
				case 'NotEqual':
				criteria.ne(propertyName, value)
				break
				case 'LessThan':
				criteria.lt(propertyName, value)
				break
				case 'LessThanEquals':
				criteria.le(propertyName, value)
				break
				case 'GreaterThan':
				criteria.gt(propertyName, value)
				break
				case 'GreaterThanEquals':
				criteria.ge(propertyName, value)
				break
				case 'Like':
				if (!value.startsWith('*')) value = "*${value}"
				if (!value.endsWith('*')) value = "${value}*"
				criteria.like(propertyName, value?.replaceAll("\\*", "%"))
				break
				case 'ILike':
				if (!value.startsWith('*')) value = "*${value}"
				if (!value.endsWith('*')) value = "${value}*"
				criteria.ilike(propertyName, value?.replaceAll("\\*", "%"))
				break
				case 'NotLike':
            	if (!value.startsWith('*')) value = "*${value}"
            	if (!value.endsWith('*')) value = "${value}*"
            	criteria.not {
            		criteria.like(propertyName, value?.replaceAll("\\*", "%"))
            	}
            	break
				case 'NotILike':
            	if (!value.startsWith('*')) value = "*${value}"
                if (!value.endsWith('*')) value = "${value}*"
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
				default:
				break
			} // end op switch
		} else {  // value is null
			added = false
		}
    	//println "== addCriterion OUT =="
    }

    /**
    * Parse the user input value to the domain property type.
    * @returns The input parsed to the appropriate type if possible, else null.
    */
    def parseValue(def domainProperty, def val, def params, def associatedPropertyParamName) {
        if(val instanceof String)
            val = val.trim()
        if (val) {
            Class cls = domainProperty.referencedPropertyType ?: domainProperty.type
            String clsName = cls.simpleName.toLowerCase()
            log.debug("domainProperty is ${domainProperty}, type is ${domainProperty.type}, refPropType is ${domainProperty.referencedPropertyType} val is ${val}, clsName is ${clsName}")

            if (domainProperty.isEnum()) {
                val = Enum.valueOf(cls, val.toString())
            } else if ("boolean".equals(clsName)) {
                val = val.toBoolean()
            } else if ( "int".equals(clsName) || "integer".equals(clsName) ) {
                val = val.isInteger() ? val.toInteger() : null
            } else if ("long".equals(clsName)) {
                val = val.isLong() ? val.toLong() : null
            } else if ("double".equals(clsName)) {
                val = val.isDouble() ? val.toDouble() : null
            } else if ("float".equals(clsName)) {
                val = val.isFloat() ? val.toFloat() : null
            } else if ("short".equals(clsName)) {
                try { val = val.toShort() } //no isShort()
                catch(java.lang.NumberFormatException e) { val = null }
            } else if ("bigdecimal".equals(clsName)) {
                val = val.isBigDecimal() ? val.toBigDecimal() : null
            } else if ("biginteger".equals(clsName)) {
                val = val.isBigInteger() ? val.toBigInteger() : null
            } else if (java.util.Date.isAssignableFrom(cls)) {
                def paramName = associatedPropertyParamName ?: domainProperty.name
                val = FilterUtils.parseDateFromDatePickerParams(paramName, params)
            }
        }
        return val
    }
}