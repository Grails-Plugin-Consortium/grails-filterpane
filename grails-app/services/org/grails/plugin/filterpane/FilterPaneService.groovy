package org.grails.plugin.filterpane


class FilterPaneService {

    boolean transactional = true
    def grailsApplication

    def filter(def params, Class filterClass) {
        return filter(params, filterClass, false)
    }

    def count(def params, Class filterClass) {
        return filter(params, filterClass, true)
    }
	
	private def filterParse(def c, def domainClass, def params, def filterParams, def filterOpParams, def doCount) {
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
					def nextFilterParams = rawValue
					def nextFilterOpParams = filterOp
		
					// Are any of the values non-empty?
					if (nextFilterOpParams.values().find {it.size() > 0} != null) {
		
						if (log.isDebugEnabled()) log.debug("== Adding association ${propName}")
		
						c."${propName}"() {
							
							def nextDomainProp = FilterPaneUtils.resolveDomainProperty(grailsApplication, domainClass, propName)
							def nextDomainClass = nextDomainProp.referencedDomainClass
							
							filterParse(c, nextDomainClass, params, nextFilterParams, nextFilterOpParams, doCount)
		
							// If they want to sort by an associated property, need to do it here.
							if (!doCount && params.sort && params.sort.startsWith("${propName}.")) {
								def parts = params.sort.split("\\.")
								if (parts.size() == 2) {
									order(parts[1], params.order ?: 'asc')
								}
							}
						} // end c.propName closure.
					} // end if any values not empty.
				} else {
					//log.debug("propName is ${propName}")
					def thisDomainProp = FilterPaneUtils.resolveDomainProperty(grailsApplication, domainClass, propName)
					def val  = this.parseValue(thisDomainProp, rawValue, filterParams, null)
					def val2 = this.parseValue(thisDomainProp, rawValue2, filterParams, "${propName}To")
					if (log.isDebugEnabled()) log.debug("== propName is ${propName}, rawValue is ${rawValue}, val is ${val} of type ${val?.class} val2 is ${val2} of type ${val2?.class}")
					this.addCriterion(c, propName, filterOp, val, val2, filterParams, thisDomainProp)
				}
			}
			if (log.isDebugEnabled()) log.debug("==============================================================================='\n")
		} // end each op
	} // end filterParse

    private def filter(def params, Class filterClass, boolean doCount) {
        if (log.isDebugEnabled()) log.debug("filtering... params = ${params.toMapString()}")
        //def filterProperties = params?.filterProperties?.tokenize(',')
        def filterParams = params.filter ? params.filter : params
        def filterOpParams = filterParams.op
        def associationList = []
        def domainClass = FilterPaneUtils.resolveDomainClass(grailsApplication, filterClass)

        //if (filterProperties != null) {
        if (filterOpParams != null && filterOpParams.size() > 0) {

            def c = filterClass.createCriteria()

            def criteriaClosure = {
                def mc = filterClass.getMetaClass()
                and {
                    filterParse(c, domainClass, params, filterParams, filterOpParams, doCount)
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
					if (params.fetchMode) {
						def fetchModes = null
						if (params.fetchMode instanceof Map)
							fetchModes = params.fetchModes

						if (fetchModes) {
							fetchModes.each { association, mode ->
								c.fetchMode(association, mode)
							}
						}
					}
 					def defaultSort = null
 					try {
 						defaultSort = filterClass?.mapping?.mapping?.sort
 					} catch (Exception ex) {
 						log.info("No mapping property found on filterClass ${filterClass}")
 					}
                    if (params.sort) {
                        if (params.sort.indexOf('.') < 0) { // if not an association..
                            order(params.sort, params.order ?: 'asc' )
                        }
						// sorting by association is now done when adding the association (filterParse)
						/* else {
                            def parts = params.sort.split("\\.")
                            if (!associationList.contains(parts[0])) {
                                c."${parts[0]}" {
                                    order(parts[1], params.order ?: 'asc')
                                }
                            }
                        }*/
                    } else if (defaultSort != null) {
						if (log.debugEnabled) log.debug('No sort specified and default is specified on domain.  Using it.')
						order(defaultSort, params.order ?: 'asc')
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
            value = FilterPaneUtils.getBeginningOfDay(value)
            value2 = FilterPaneUtils.getEndOfDay(value)
            if (log.isDebugEnabled())
                log.debug("Date criterion is Equal to day precision.  Changing it to between ${value} and ${value2}")
        }

		if(value != null) {
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
    }

    /**
    * Parse the user input value to the domain property type.
    * @returns The input parsed to the appropriate type if possible, else null.
    */
    def parseValue(def domainProperty, def val, def params, def associatedPropertyParamName) {
        if(val instanceof String) {
            val = val.trim()
        }
			
		// GRAILSPLUGINS-1717.  Groovy truth treats empty strings as false.  Compare against null.
		if (val != null) {
            Class cls = domainProperty.referencedPropertyType ?: domainProperty.type
            String clsName = cls.simpleName.toLowerCase()
            log.debug("cls is enum? ${cls.isEnum()}, domainProperty is ${domainProperty}, type is ${domainProperty.type}, refPropType is ${domainProperty.referencedPropertyType} val is '${val}', clsName is ${clsName}")

            if (domainProperty.isEnum()) {
				def tempVal = val
				val = null // default to null.  If it's valid, it'll get replaced with the real value.
				try {
					if (tempVal.toString().length() > 0) {
	                	val = Enum.valueOf(cls, tempVal.toString())
					}
				} catch(IllegalArgumentException iae) {
					log.debug("Enum valueOf failed.  val is ${tempVal}")
					// Ignore this.  val is not a valid enum value (probably an empty string).
				}
            } else if ("boolean".equals(clsName)) {
                val = val.toBoolean()
            } else if ( "int".equals(clsName) || "integer".equals(clsName) ) {
                val = val.isInteger() ? val.toInteger() : null
            } else if ("long".equals(clsName)) {
                try { val = val.toLong() } //no isShort()
                catch(java.lang.NumberFormatException e) { val = null }
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
                val = FilterPaneUtils.parseDateFromDatePickerParams(paramName, params)
            } else if ("currency".equals(clsName)) {
				try {
					val = Currency.getInstance(val)
				} catch (IllegalArgumentException iae) {
					// Do nothing.  
				}
			}
        }
        return val
    }
}