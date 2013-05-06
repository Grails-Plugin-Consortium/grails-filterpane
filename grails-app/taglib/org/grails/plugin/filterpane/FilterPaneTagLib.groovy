package org.grails.plugin.filterpane

import org.springframework.web.servlet.support.RequestContextUtils

/**
 * @author skrenek
 *
 */
class FilterPaneTagLib {

    static namespace = 'filterpane'

    private static final String LAST_UPDATED = 'lastUpdated'
    private static final String DefaultFilterPaneId = 'filterPane'
    private static final String DEFAULT_FORM_METHOD = 'post'

    /**
     * This map contains available filter operations by type.  It is used when creating the
     * individual rows in the filter pane.  The values in the text maps are key suffixes for the
     * resource bundle.  The prefix used in the valueMessagePrefix attribute will be fp.op.
     */
    private static final Map availableOpsByType = [
            text: ['', FilterPaneOperationType.ILike.operation, FilterPaneOperationType.NotILike.operation, 
                    FilterPaneOperationType.Like.operation, FilterPaneOperationType.NotLike.operation, 
                    FilterPaneOperationType.Equal.operation, FilterPaneOperationType.NotEqual.operation, 
                    FilterPaneOperationType.IsNull.operation, FilterPaneOperationType.IsNotNull.operation],
            numeric: ['', FilterPaneOperationType.Equal.operation, FilterPaneOperationType.NotEqual.operation, 
                    FilterPaneOperationType.LessThan.operation, FilterPaneOperationType.LessThanEquals.operation, 
                    FilterPaneOperationType.GreaterThan.operation,
                    FilterPaneOperationType.GreaterThanEquals.operation, FilterPaneOperationType.Between.operation,
                    FilterPaneOperationType.IsNull.operation, FilterPaneOperationType.IsNotNull.operation],
            date: ['', FilterPaneOperationType.Equal.operation, FilterPaneOperationType.NotEqual.operation,
                    FilterPaneOperationType.LessThan.operation, FilterPaneOperationType.LessThanEquals.operation,
                    FilterPaneOperationType.GreaterThan.operation,
                    FilterPaneOperationType.GreaterThanEquals.operation, FilterPaneOperationType.Between.operation,
                    FilterPaneOperationType.IsNull.operation, FilterPaneOperationType.IsNotNull.operation],
            'boolean': ['', FilterPaneOperationType.Equal.operation, FilterPaneOperationType.NotEqual.operation,
                    FilterPaneOperationType.IsNull.operation, FilterPaneOperationType.IsNotNull.operation],
            'enum': ['', FilterPaneOperationType.Equal.operation, FilterPaneOperationType.NotEqual.operation],
            currency: ['', FilterPaneOperationType.Equal.operation, FilterPaneOperationType.NotEqual.operation]
    ]

    /**
     * This tag generates necessary style and script includes.  Use this tag in the head element
     * of your pages.<br/>
     * <br/>
     * Attributes
     * <ul>
     * <li>css - add this attribute to generate stylesheet includes (any value is fine)</li>
     * <li>js - add this attribute to generate javascript includes (any value is fine)</li>
     * <li>(none) - Using the tag with no attributes is identical to adding both. (&lt;filterpane:includes css="true" js="true" /&gt;</li>
     * </ul>
     *
     * @since 0.4; attributes since 2.0
     * @since 2.1.6; resources plugin now standard
     * @deprecated
     */
    def includes = { attrs, body ->
        log.error 'includes tag is now deprecated, please include the `filterpane` resources module instead.'
        boolean showCss = false
        boolean showJs = false
        if(attrs != null && attrs.size() > 0) {
            showCss = (attrs.css != null)
            showJs = (attrs.js != null)
        } else {
            showCss = true
            showJs = true
        }

        if(showCss)
            out << "<link rel=\"stylesheet\" type=\"text/css\" href=\"${g.resource(dir: 'css', plugin: 'filterpane', file: 'fp.css')}\" />\n"

        if(showJs)
            out << "<script type=\"text/javascript\" src=\"${g.resource(dir: 'js', plugin: 'filterpane', file: 'fp.js')}\"></script>"
    }

    def isFiltered = { attrs, body ->
        if(FilterPaneUtils.isFilterApplied(params)) {
            out << body()
        }
    }

    def isNotFiltered = { attrs, body ->
        if(!FilterPaneUtils.isFilterApplied(params)) {
            out << body()
        }
    }

    /**
     * Creates a link (button) that displays the filter pane when pressed.  The title attribute
     * may be used to modify the text on the button.  If omitted, the text will be "Filter".
     * The filterPaneId attribute may be specified if the associated filterPane tag has an id attribute.
     * Their values must be the same.
     */
    def filterButton = { attrs, body ->
        def renderModel = [:]
        renderModel.filterPaneId = attrs.filterPaneId ?: DefaultFilterPaneId
        renderModel.styleClass = attrs.class ?: ''
        renderModel.style = attrs.style ?: ''

        if(FilterPaneUtils.isFilterApplied(params)) {
            renderModel.styleClass = "filter-applied ${renderModel.styleClass}"
            renderModel.text = resolveAttribute(attrs.appliedTextKey, "fp.tag.filterButton.appliedText", attrs.appliedText, "Filter")

        } else {
            renderModel.text = resolveAttribute(attrs.textKey, "fp.tag.filterButton.text", attrs.text, "Filter")
        }
        out << g.render(template: "/filterpane/filterButton", model: renderModel)
    }

    /**
     * Grails pagination tag wrapper for use with the filterpane plugin.
     *
     * attribute total - a custom count to be used in pagination
     * attribute domainBean - the domain bean being filtered.  Ignored if total is specified.
     */
    def paginate = { attrs, body ->
        def filterParams = FilterPaneUtils.extractFilterParams(params)
        def count = 0I

        if(attrs.total != null) {
            count = attrs.total
        } else if(attrs.domainBean) {
            def dc = FilterPaneUtils.resolveDomainClass(grailsApplication, attrs.domainBean)

            if(dc) count = dc.clazz.count()
        }
        attrs.total = count
        attrs.params = filterParams
        out << g.paginate(attrs, body)
    }

    def currentCriteria = { attrs, body ->
        def renderModel = [:]
        boolean useFullAssociationPath = resolveBoolAttrValue(attrs.fullAssociationPathFieldNames ?: 'y')
        renderModel.isFiltered = FilterPaneUtils.isFilterApplied(params)
        if(renderModel.isFiltered == true) {
            renderModel.id = attrs.id ?: 'filterPaneCurrentCriteria'
            renderModel.quoteValues = resolveBoolAttrValue(attrs.quoteValues ?: 'y')
            renderModel.title = attrs.title ?: ''
            renderModel.styleClass = attrs['class'] ?: ''
            renderModel.style = attrs.style ?: ''
            renderModel.dateFormat = attrs.dateFormat ?: 'yyyy-MM-dd HH:mm:ss'
            renderModel.criteria = []
            renderModel.removeImgDir = attrs.removeImgDir ?: ''
            renderModel.removeImgFile = attrs.removeImgFile
            renderModel.action = attrs.action ?: 'filter'

            def filterParams = FilterPaneUtils.extractFilterParams(params, true)
            def domainBean = FilterPaneUtils.resolveDomainClass(grailsApplication, attrs.domainBean)

            def getProp = { key, filterOp ->
                if(key.startsWith('filter.op') && filterOp != null && filterOp != '') {return key[10..-1]}
                false
            }

            def getDomainProp = { prop ->
                if(prop.contains('.')) { // association.
                    def parts = prop.toString().split('\\.')
                    def domainProp
                    def domainObj = domainBean
                    int lastPartIndex = parts.size() - 1
                    for(int i = 0; i < lastPartIndex; i++) {
                        domainObj = domainObj.getPropertyByName(parts[i]).referencedDomainClass
                    }

                    return domainObj.getPropertyByName(parts[lastPartIndex])
                }

                domainBean.getPropertyByName(prop)
            }

            //log.debug("=================================================================")
            //log.debug("current criteria filterParams: ${filterParams}")

            filterParams.each { key, filterOp ->

                def criteriaModel = [:]
                def prop = getProp(key, filterOp)

                if(prop) {
                    def domainProp = getDomainProp(prop)
                    def filterValue = filterParams["filter.${prop}"]
                    def filterValueTo = null
                    boolean isNumericType = (domainProp.referencedPropertyType
                                             ? Number.isAssignableFrom(domainProp.referencedPropertyType)
                                             : false)
                    boolean isNumericAndBlank = isNumericType && !"".equals(filterValue.toString().trim())
                    boolean isDateType = (domainProp.referencedPropertyType
                                          ? Date.isAssignableFrom(domainProp.referencedPropertyType)
                                          : false)
                    boolean isEnumType = domainProp?.referencedPropertyType?.isEnum()
                    if(filterValue != null && (!isNumericType || isNumericAndBlank) && filterOp?.size() > 0) {

                        if(isDateType) {
                            filterValue = FilterPaneUtils.parseDateFromDatePickerParams("filter.${prop}", filterParams)
                            if(filterValue) {
                                def df = renderModel.dateFormat
                                if(df instanceof Map) {
                                    df = renderModel.dateFormat[prop]
                                }
                                filterValue = g.formatDate(format: df, date: filterValue)
                            }
                        } else if(isEnumType) {
                            def tempMap = [:]
                            addFilterPropertyValues(attrs, tempMap, prop)
                            def enumValue = Enum.valueOf(domainProp.referencedPropertyType, filterValue.toString())
                            if(enumValue && tempMap.displayProperty) {
                                filterValue = tempMap.displayProperty == 'name' ? enumValue.name() : enumValue[tempMap.displayProperty]
                            }

                        }

                        def lcFilterOp = filterOp.toString().toLowerCase()
                        switch(lcFilterOp) {

                            case FilterPaneOperationType.IsNull.operation:
                            case FilterPaneOperationType.IsNotNull.operation:
                                filterValue = ''
                                break
                            case FilterPaneOperationType.Between.operation:
                                filterValueTo = filterParams["filter.${prop}To"]
                                if(filterValueTo == 'struct') {
                                    filterValueTo = FilterPaneUtils.parseDateFromDatePickerParams("filter.${prop}To", params)
                                    if(filterValueTo) {
                                        def df = renderModel.dateFormat
                                        if(df instanceof Map) {
                                            df = renderModel.dateFormat[prop]
                                        }
                                        filterValueTo = g.formatDate(format: df, date: filterValueTo)
                                    }
                                }
                                break
                        } // end switch.

                        criteriaModel.filterOp = filterOp
                        criteriaModel.filterValue = filterValue
                        criteriaModel.filterValueTo = filterValueTo
                        criteriaModel.params = [:]
                        criteriaModel.params.putAll(filterParams)
                        criteriaModel.params.sort = params.sort
                        criteriaModel.params.order = params.order
                        criteriaModel.params[key] = '' // <== This is what removes the criteria from the list.
                        criteriaModel.domainProp = domainProp // <-- TODO: look at this and resolve it how it is done on filterPane tag.
                        criteriaModel.prop = prop
                        criteriaModel.fieldName = resolveFieldName(prop, domainProp, prop.contains('.'), useFullAssociationPath)
                        //log.debug("=================================================================")
                        //log.debug("criteriaModel: ${criteriaModel}")
                        renderModel.criteria << criteriaModel
                    } // end if fv != null
                }
            }
            //log.debug("=================================================================")
            //log.debug("renderModel: ${renderModel}")
            out << g.render(template: "/filterpane/currentCriteria", model: renderModel)
        }

    }

    def filterPane = { attrs, body ->

        if(!attrs.domain) {
            log.error("domain attribute is required")
            return
        }

        def renderModel = [customForm: false]

        // Validate required info
        def domain = FilterPaneUtils.resolveDomainClass(grailsApplication, attrs.domain)
        if(domain == null) {
            log.error("Unable to resolve domain class for ${attrs.domain}")
            return
        }

        boolean useFullAssociationPath = resolveBoolAttrValue(attrs.fullAssociationPathFieldNames ?: 'y')

        // Set up the render model.

        renderModel.title = resolveAttribute(attrs.titleKey, 'fp.tag.filterPane.titleText', attrs.title, 'Filter')
        renderModel.containerId = attrs.id ?: DefaultFilterPaneId
        renderModel.containerIsDialog = resolveBoolAttrValue(attrs.dialog) ? " fp-dialog" : ""
        renderModel.containerVisibleStyle = resolveBoolAttrValue(attrs.visible) ? "" : "display:none;"
        renderModel.containerClass = attrs['class'] ?: (attrs.styleClass ?: '')
        renderModel.containerStyle = attrs.style ?: ''
        renderModel.formName = attrs.formName ?: 'filterPaneForm'
        renderModel.formMethod = attrs.formMethod ?: DEFAULT_FORM_METHOD
        renderModel.controller = attrs.controller
        renderModel.action = attrs.action ?: 'filter'
        renderModel.customForm = "true".equalsIgnoreCase(attrs?.customForm) || attrs?.customForm == true
        renderModel.formAction = renderModel.controller ?
                                 g.createLink(controller: renderModel.controller, action: renderModel.action) :
                                 renderModel.action;
        renderModel.showSortPanel = attrs.showSortPanel ? resolveBoolAttrValue(attrs.showSortPanel) : true
        renderModel.showButtons = attrs.showButtons ? resolveBoolAttrValue(attrs.showButtons) : true
        renderModel.showTitle = attrs.showTitle ? resolveBoolAttrValue(attrs.showTitle) : true
        renderModel.listDistinct = attrs.listDistinct ? resolveBoolAttrValue(attrs.listDistinct) : false
        renderModel.uniqueCountColumn = attrs.uniqueCountColumn ?: ''

        /*
           * Need properties to filter,
           * associated properties
           * additional properties
           * excluded properties 
           */

        def finalProps = [:]
        List persistentProps = domain.persistentProperties as List
        List additionalPropNames = resolveListAttribute(attrs.additionalProperties)
        List excludePropNames = resolveListAttribute(attrs.excludeProperties)
        List associatedPropNames = resolveListAttribute(attrs.associatedProperties)

        // DEPRECATION: attribute name filterProperties as of 2.0
        List explicitPropNames = resolveListAttribute(attrs.explicitProperties ?: attrs.filterProperties)

        // If they explicitly requested certain props, remove the others.
        if(explicitPropNames.size() > 0) {
            persistentProps = persistentProps.findAll {
                explicitPropNames.contains(it.name)
            }
        }

        // Extract out the associations.  These are handled separately from simple properties.
        List associatedProps = persistentProps.findAll {
            it.association == true && !it.type.isEnum()
        }
        persistentProps.removeAll(associatedProps)


        def lastUpdatedProp = persistentProps.find {
            it.name == LAST_UPDATED
        }

        if(lastUpdatedProp != null) {

            // Verify they did not explicitly request it before removing it.
            boolean keep = explicitPropNames.contains(LAST_UPDATED) || additionalPropNames.contains(LAST_UPDATED)
            if(!keep) {
                persistentProps.remove(lastUpdatedProp)
            }
        }

        // Only id and version are supported right now.
        for(def ap : additionalPropNames) {
            if("id".equals(ap) || "identifier".equals(ap)) {
                finalProps[domain.identifier] = domain.identifier.name
            } else if("version".equals(ap)) {
                finalProps[domain.version] = domain.version.name
            }
        }

        excludePropNames.each { name ->
            def epObj = persistentProps.find {
                it.name == name
            }
            if(epObj != null) {
                log.debug "Removing ${name} from final props"
                persistentProps.remove(epObj)
            }
        }

        // Construct the final properties to filter
        persistentProps.each { finalProps[it] = it.name }

        // Add the associated properties
        associatedPropNames.each { dottedName ->
            addAssociatedProperty(finalProps, dottedName, associatedProps)
        }

        log.debug "${finalProps.size()} final props: ${finalProps}"

        // sortedProps is a list of GrailsDomainClassProperty instances, sorted by order they appear in the GrailsDomainClass 
        def sortedProps = finalProps.keySet().asList().sort(new org.codehaus.groovy.grails.scaffolding.DomainClassPropertyComparator(domain))

        renderModel.properties = []

        mapSortedProps(sortedProps, finalProps, attrs, useFullAssociationPath, renderModel)

        def sortKeys = []
        sortedProps.each { sp ->
            def name = (finalProps.find { sp.name == it.value })?.value
            if(name) sortKeys << name
        }
        associatedPropNames.each { apn -> sortKeys << apn }
        log.debug("Sorted props: ${sortedProps}")
        log.debug("Sort keys: ${sortKeys}")

        assignRenderModels(attrs, sortedProps, sortKeys, renderModel)

        out << g.render(template: "/filterpane/filterpane", model: [fp: renderModel])
    }

    def date = { attrs, body ->

        Date d = FilterPaneUtils.parseDateFromDatePickerParams(attrs.name, params)
        def model = [:]
        model.putAll(attrs)
        model.value = d
        model.onChange = "grailsFilterPane.selectDefaultOperator('${attrs.opName}')"
        model.isDayPrecision = (attrs.precision == 'day') ? 'y' : 'n'
        def ret = g.render(template: "/filterpane/dateControl", model: [ctrlAttrs: model])
        out << ret
    }

    def bool = { attrs, body ->
        def ret = g.render(template: "/filterpane/boolean", model: attrs)
        out << ret
    }

    def input = { attrs, body ->

        def ret = null

        if(attrs?.ctrlType) {
            switch(attrs.ctrlType) {
                case 'date':
                    ret = date(attrs.ctrlAttrs)
                    break
                case 'bool':
                case 'boolean':
                    ret = bool(attrs.ctrlAttrs)
                    break
                case 'select':
                    ret = g.select(attrs.ctrlAttrs)
                    break
                case 'text':
                    ret = g.textField(attrs.ctrlAttrs)
                    break
                default:
                    ret = "<-- Unknown control type: ${attrs?.ctrlType} -->"
                    break
            }
        }

        out << (ret ?: '')
    }

    /**
     * Creates a quick filter html link that links to the specified filter action,
     * listing items filtered upon the specified values.
     * Example usage:<br/>
     * In the <tt>show</tt> view this tag can be used to link to a list of related child objects.
     * The effect is that when clicking on the link that the tag creates,
     * the child object's list view is displayed, filtered upon the parent id:<br/>
     * <br/>
     * <pre>
     * ...
     * <filterpane:filterLink values="${['author.id' : authorInstance.id]}" controller="book">
     *  <g:message code="author.books.label" default="Books by this author" />
     * </filterpane:filterLink>
     * ...
     * </pre>
     * @param values
     *          A map containing field values by field name. The field is the field within the bean (with the filter) that should be used for filtering. 
     *          Optionally, instead of the field value, you can supply a map like this [op:<The filter operator>, value:<the filter value>]. This map may
     *          optionally also contain the key "to" if the op operator is "Between".
     * @param controller
     *          The controler that contains the action to use. Optional, default is current controller.
     * @param action
     *          The action to use for filtering. Optional, default is <i>filter</i>.
     * @param *
     *          Additionally you may use all the optional parameters/attrs that you can use to the tag g:link.         
     * @body The body of this tag should contain the text to display within the link.
     */
    def filterLink = { attrs, body ->
        def filterParams = attrs.filterParams;
        def values = attrs.values;
        def label = body();
        def controller = attrs.controller;
        def action = attrs.action ?: 'filter';

        def linkParams = [:];
        if(filterParams) {linkParams.putAll(filterParams);}
        if(!values) {throw new IllegalArgumentException("Mandatory argument 'values' is missing.")}
        if(!values instanceof Map) {throw new IllegalArgumentException("Mandatory argument 'values' needs to be of type Map.")}
        linkParams.sort = params.sort
        linkParams.order = params.order
        for(String field : values.keySet()) {
            def value = values[field]

            if(value instanceof Map && value?.op && !FilterPaneOperationType.getFilterPaneOperationType(value.op)){
                throw new RuntimeException("Operation type ${value.op} is not supported.  Please see FilterPaneOperationType for supported operations.")
            }

            if(value == null || value == 'null') {
                linkParams['filter.op.' + field] = FilterPaneOperationType.IsNull.operation;
                linkParams['filter.' + field] = '0';
            }
            else if(value instanceof Map) {
                if(value.op == FilterPaneOperationType.IsNull.operation || value.op == FilterPaneOperationType.IsNotNull.operation) {value.value = '0'}
                linkParams['filter.op.' + field] = value.op ?: FilterPaneOperationType.Equal.operation;
                linkParams['filter.' + field] = value.value ?: value.from;
                if(value.to) {linkParams["filter.${field}To"] = value.to;}

            }
            else {
                linkParams['filter.op.' + field] = FilterPaneOperationType.Equal.operation;
                // Find the value also for referenced child objects
                linkParams['filter.' + field] = value;
            }
        }

        def linkAttrs = [action: action]
        linkAttrs.putAll(attrs)
        linkAttrs.remove('values')
        linkAttrs.remove('filterParams')
        linkAttrs.params = linkParams

        out << g.link(linkAttrs) { label };
    }

    private void assignRenderModels(Map attrs, List sortedProps, ArrayList sortKeys, LinkedHashMap<String, Boolean> renderModel) {
        renderModel.sortModel = [sortValueMessagePrefix: attrs.sortValueMessagePrefix ?: null,
                sortedProperties: sortedProps,
                sortKeys: sortKeys,
                sortValue: params.sort ?: "",
                noSelection: ['': g.message(code: 'fp.tag.filterPane.sort.noSelection.text', default: 'Select a Property')],
                orderAsc: params.order == 'asc',
                orderDesc: params.order == 'desc']

        renderModel.buttonModel = [
                cancelText: g.message(code: 'fp.tag.filterPane.button.cancel.text', default: 'Cancel'),
                clearText: g.message(code: 'fp.tag.filterPane.button.clear.text', default: 'Clear'),
                applyText: g.message(code: 'fp.tag.filterPane.button.apply.text', default: 'Apply'),
                action: renderModel.action,
                containerId: renderModel.containerId,
                formName: renderModel.formName]
    }

    private void mapSortedProps(List sortedProps, finalProps, attrs, boolean useFullAssociationPath, renderModel) {
        sortedProps.each { sp ->
            def map = [domainProperty: sp]
            def propertyKey = finalProps[sp]
            def opName = "filter.op.${propertyKey}"
            def propertyType = sp.type
            def type = FilterPaneUtils.getOperatorMapKey(propertyType)


            def name = "filter.${propertyKey}"
            map.ctrlAttrs = [name: name, value: params[name], opName: opName, domain: sp.domainClass.name, propertyName: sp.name]//, domainProperty:sp]
            addFilterPropertyValues(attrs, map.ctrlAttrs, propertyKey)

            def opKeys = []
            opKeys.addAll(availableOpsByType[type])

            // If the property is not nullable, no need to allow them to filter
            // in is or is not null.
            def constrainedProperty = sp.domainClass.constrainedProperties[sp.name]
            if((constrainedProperty && !constrainedProperty.isNullable()) || sp.name == 'id') {
                opKeys.remove(FilterPaneOperationType.IsNotNull.operation)
                opKeys.remove(FilterPaneOperationType.IsNull.operation)
            }

            map.ctrlType = "text"
            if(propertyType.isAssignableFrom(Date.class)) {
                map.ctrlType = "date"
            } else if(propertyType == Boolean.class || propertyType == boolean.class) {
                map.ctrlType = "boolean"
            }

            // If the user did not specify a value list and the property is
            // constrained with one, use the domain class's list.
            if(!map.ctrlAttrs.values) {
                List inList = constrainedProperty?.getInList()
                if(inList) {
                    map.ctrlAttrs.values = inList
                }
                else if(sp.type.isEnum()) {
                    //map.ctrlAttrs.values = sp.type.enumConstants as List
                    def valueList = []

                    sp.type.enumConstants.each {
                        def value = it
                        if(map.ctrlAttrs.valueProperty) {
                            value = map.ctrlAttrs.valueProperty == 'ordinal' ? it.ordinal() : it[map.ctrlAttrs.valueProperty]
                        }
                        def display = value
                        if(map.ctrlAttrs.displayProperty) {
                            display = map.ctrlAttrs.displayProperty == 'name' ? it.name() : it[map.ctrlAttrs.displayProperty]
                        }
                        valueList << [id: value, name: display]
                    }
                    map.ctrlAttrs.values = valueList
                    map.ctrlAttrs.optionKey = "id"
                    map.ctrlAttrs.optionValue = "name"
                } // end else if
            } else { // end if no values specified
                def valueToken = map.ctrlAttrs.valuesToken ?: ' '
                map.ctrlAttrs.values = map.ctrlAttrs.values instanceof List ? map.ctrlAttrs.values : map.ctrlAttrs.values.tokenize(valueToken)
            }

            // If the values list is now specified, limit the operators to == or <>
            if(map.ctrlAttrs.values) {
                opKeys = ['', FilterPaneOperationType.Equal.operation, FilterPaneOperationType.NotEqual.operation]

                // Also set up the rest of the dropdown ctrl attrs
                map.ctrlAttrs.from = map.ctrlAttrs.values
                map.ctrlAttrs.remove('values') // transferred to "from" property
                map.ctrlAttrs.noSelection = ['': '']

                def valueMessagePrefix = map.ctrlAttrs.valueMessagePrefix ?: "fp.property.text.${sp.name}"
                def valueMessageAltPrefix = "${sp.domainClass.propertyName}.${sp.name}"
                def messageSource = grailsAttributes.getApplicationContext().getBean("messageSource")
                def locale = RequestContextUtils.getLocale(request)
                if(messageSource.getMessage(valueMessagePrefix, null, null, locale) != null) {
                    map.ctrlAttrs.valueMessagePrefix = valueMessagePrefix
                } else if(messageSource.getMessage(valueMessageAltPrefix, null, null, locale) != null) {
                    map.ctrlAttrs.valueMessagePrefix = valueMessageAltPrefix
                }
                map.ctrlType = "select"
            }

            if(map.ctrlType == 'select' || map.ctrlType == 'text') {
                map.ctrlAttrs.onChange = "grailsFilterPane.selectDefaultOperator('${opName}')"
            }

            // Create the operator dropdown attributes.
            map.opName = opName
            map.opKeys = opKeys
            map.opValue = params[opName]
//			def opDropdown = this.select(id: opName, name: opName, from: opKeys, keys: opKeys,
//			value: params[opName], valueMessagePrefix:'fp.op',
//			onChange: "filterOpChange('${opName}', '${map.ctrlAttrs.id}');")
            if(params[opName] == FilterPaneOperationType.IsNull.operation || params[opName] == "IsNotNull") {
                map.ctrlAttrs.style = 'display:none;'
            }

            // Note: propertyKey is the dotted name
            def fieldName = resolveFieldName(propertyKey, sp, propertyKey.contains('.'), useFullAssociationPath)

            map.fieldLabel = fieldName

            // Add this new field name as a property of this instance
            sp.metaClass.getFilterPaneFieldName = {-> new String(fieldName) }

            // For numeric and date types, build the "To" control, in case they select between.
            if(type == "numeric" || type == "date") {
                map.toCtrlAttrs = [:]
                map.toCtrlAttrs.putAll(map.ctrlAttrs)
                map.toCtrlAttrs.name += "To"
                map.toCtrlAttrs.id += "To"
                map.toCtrlAttrs.value = params[map.toCtrlAttrs.name]

                boolean showToCtrl = "between".equalsIgnoreCase(params[opName])
                if(showToCtrl) {
                    showToCtrl = (map.toCtrlAttrs.value instanceof Date) ? map.toCtrlAttrs.value != "" : map.toCtrlAttrs.value?.trim() != ""
                }
                map.toCtrlSpanStyle = showToCtrl ? "" : "display:none;"
            } // end if type numeric or date

            renderModel.properties << map
        }
    }

    /**
     * Resolve the value given the list of possible sources.  Order is as follows:<br/>
     * <ol>
     * <li>Message bundle, via "customKey"</li>
     * <li>attr value</li>
     * <li>Message bundle, via "localizationKey"</li>
     * <li>default value</li>
     * </ol>
     *
     * @param customKey
     * @param localizationKey
     * @param attrValue
     * @param defaultValue
     * @return
     */
    private def resolveAttribute(String customKey, String localizationKey, attrValue, String defaultValue) {
        def result

        if(customKey) {
            result = g.message(code: customKey, default: defaultValue)
        } else {
            result = attrValue ?: g.message(code: localizationKey, default: defaultValue)
        }

        result ?: defaultValue
    }

    private List resolveListAttribute(attr) {
        List temp = []
        if(attr != null) {
            if(attr instanceof List) {
                temp = attr
            } else if(attr instanceof String) {
                temp = attr.split(",") as List
            }
        }
        temp.collect { it.trim() }
    }

    private void addAssociatedProperty(def finalProps, String dottedName, List associatedProps) {

        List parts = dottedName.split('\\.') as List
        def association = associatedProps.find { it.name == parts[0] }
        def refDomain = null
        def refProperty = null
        int index = 1
        def fieldNamePrefix = ""

        while(association && index < parts.size()) {
            refDomain = association.referencedDomainClass
            fieldNamePrefix += "${grails.util.GrailsNameUtils.getNaturalName(refDomain.clazz.simpleName)}'s "
            refProperty = ("id".equalsIgnoreCase(parts[index]) || "identifier".equalsIgnoreCase(parts[index])) ?
                          refDomain.identifier :
                          refDomain.persistentProperties.find { it.name == parts[index] }
            //log.debug("refDomain is ${refDomain}, refProperty is ${refProperty}, parts[${index}] = ${parts[index]}")
            association = (refProperty?.association == true && refProperty?.type?.isEnum() == false) ? refProperty : null
            index += 1
        }

        if(refProperty && !refProperty.association) {
            log.debug("adding association ${dottedName}")
            refProperty.metaClass.getFilterPaneFieldNamePrefix = {-> return fieldNamePrefix }
            finalProps[refProperty] = dottedName
        }
    }

    private def addFilterPropertyValues(def tagAttrs, def ctrlAttrs, def propertyKey) {
        if(tagAttrs.filterPropertyValues && tagAttrs.filterPropertyValues[propertyKey]) {ctrlAttrs.putAll(tagAttrs.filterPropertyValues[propertyKey])}

        if(!ctrlAttrs.id) {
            ctrlAttrs.id = propertyKey
        }
    }

    private Boolean resolveBoolAttrValue(String attr) {
        ['y', 't', 'yes', 'true'].contains(attr?.toLowerCase())
    }

    private String resolveFieldName(def propName, def sp, boolean isAssociation, boolean useFullAssociationPath) {
        // Take care of the name (label).  Yuck!
        def fieldNameKey = "fp.property.text.${propName}" // Default.
        def fieldNameAltKey = fieldNameKey // default for alt key.
        def fieldNamei18NTemplateKey = "${sp.domainClass.name}.${sp.name}"
        def fieldName = sp.naturalName

        if(isAssociation) { // association.
            fieldNameKey = "fp.property.text.${sp.domainClass.propertyName}.${sp.name}"
            fieldNamei18NTemplateKey = "${sp.domainClass.propertyName}.${sp.name}"
            // GRAILSPLUGINS-2027 Fix.  associated properties displaying package name.
            def prefix = ""
            if(sp.filterPaneFieldNamePrefix && useFullAssociationPath) {
                prefix = sp.filterPaneFieldNamePrefix
            } else {
                prefix = "${grails.util.GrailsNameUtils.getNaturalName(sp.domainClass.clazz.simpleName)}'s "
            }
            fieldName = "${prefix}${fieldName}"
        }

        /*
        log.debug("fieldNameKey is ${fieldNameKey}")
        log.debug("fieldNameAltKey is ${fieldNameAltKey}")
        log.debug("fieldNamei18NTemplateKey is ${fieldNamei18NTemplateKey}")
        log.debug("fieldName is ${fieldName}")
        */

        g.message(code: fieldNameKey, default: g.message(code: fieldNameAltKey, default: g.message(code: fieldNamei18NTemplateKey, default: fieldName)))
    }
}
