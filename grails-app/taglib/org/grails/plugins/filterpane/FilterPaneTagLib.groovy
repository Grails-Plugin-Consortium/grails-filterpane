package org.grails.plugins.filterpane

import grails.core.GrailsApplication
import grails.plugins.GrailsPluginManager
import grails.util.GrailsNameUtils
import org.apache.commons.lang.StringUtils
import org.grails.datastore.mapping.model.types.Association
import org.grails.web.gsp.io.GrailsConventionGroovyPageLocator
import org.joda.time.*
import org.joda.time.base.AbstractInstant
import org.joda.time.base.AbstractPartial
import org.springframework.web.servlet.support.RequestContextUtils

import java.lang.reflect.Modifier

import static org.grails.io.support.GrailsResourceUtils.appendPiecesForUri

/**
 * @author skrenek
 */
class FilterPaneTagLib {

    static namespace = 'filterpane'

    GrailsApplication grailsApplication

    GrailsPluginManager pluginManager
    GrailsConventionGroovyPageLocator groovyPageLocator

    private static final String LAST_UPDATED = 'lastUpdated'
    private static final String DefaultFilterPaneId = 'filterPane'
    private static final String DEFAULT_FORM_METHOD = 'post'

    /**
     * This map contains available filter operations by type.  It is used when creating the
     * individual rows in the filter pane.  The values in the text maps are key suffixes for the
     * resource bundle.  The prefix used in the valueMessagePrefix attribute will be fp.op.
     */
    private static final Map availableOpsByType = [
            class    : ['', FilterPaneOperationType.InList.operation, FilterPaneOperationType.NotInList.operation],
            text     : ['', FilterPaneOperationType.ILike.operation, FilterPaneOperationType.NotILike.operation,
                        FilterPaneOperationType.Like.operation, FilterPaneOperationType.NotLike.operation,
                        FilterPaneOperationType.Equal.operation, FilterPaneOperationType.NotEqual.operation,
                        FilterPaneOperationType.IsNull.operation, FilterPaneOperationType.IsNotNull.operation,
                        FilterPaneOperationType.IBeginsWith.operation, FilterPaneOperationType.BeginsWith.operation,
                        FilterPaneOperationType.IEndsWith.operation, FilterPaneOperationType.EndsWith.operation],
            numeric  : ['', FilterPaneOperationType.Equal.operation, FilterPaneOperationType.NotEqual.operation,
                        FilterPaneOperationType.LessThan.operation, FilterPaneOperationType.LessThanEquals.operation,
                        FilterPaneOperationType.GreaterThan.operation,
                        FilterPaneOperationType.GreaterThanEquals.operation, FilterPaneOperationType.Between.operation,
                        FilterPaneOperationType.IsNull.operation, FilterPaneOperationType.IsNotNull.operation],
            date     : ['', FilterPaneOperationType.Equal.operation, FilterPaneOperationType.NotEqual.operation,
                        FilterPaneOperationType.LessThan.operation, FilterPaneOperationType.LessThanEquals.operation,
                        FilterPaneOperationType.GreaterThan.operation,
                        FilterPaneOperationType.GreaterThanEquals.operation, FilterPaneOperationType.Between.operation,
                        FilterPaneOperationType.IsNull.operation, FilterPaneOperationType.IsNotNull.operation],
            'boolean': ['', FilterPaneOperationType.Equal.operation, FilterPaneOperationType.NotEqual.operation,
                        FilterPaneOperationType.IsNull.operation, FilterPaneOperationType.IsNotNull.operation],
            'enum'   : ['', FilterPaneOperationType.InList.operation, FilterPaneOperationType.NotInList.operation],
            currency : ['', FilterPaneOperationType.Equal.operation, FilterPaneOperationType.NotEqual.operation]
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
        if (attrs != null && attrs.size() > 0) {
            showCss = (attrs.css != null)
            showJs = (attrs.js != null)
        } else {
            showCss = true
            showJs = true
        }

        if (showCss) {
            out << "<asset:stylesheet src=\"fp.css\"/>\n"
        }

        if (showJs) {
            out << "<asset:javascript src=\"fp.js\"/>"
        }
    }

    def isFiltered = { attrs, body ->
        if (FilterPaneUtils.isFilterApplied(params)) {
            out << body()
        }
    }

    def isNotFiltered = { attrs, body ->
        if (!FilterPaneUtils.isFilterApplied(params)) {
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

        if (FilterPaneUtils.isFilterApplied(params)) {
            renderModel.styleClass = "filter-applied ${renderModel.styleClass}"
            renderModel.text = resolveAttribute(attrs.appliedTextKey, "fp.tag.filterButton.appliedText", attrs.appliedText, "Filter")

        } else {
            renderModel.text = resolveAttribute(attrs.textKey, "fp.tag.filterButton.text", attrs.text, "Filter")
        }

        Map template = getTemplatePath('filterButton')

        out << g.render(template: template.path, plugin: template.plugin, model: renderModel)
    }

    Map<String, String> getTemplatePath(String templateName) {
        def path = appendPiecesForUri("/_filterpane", templateName)
        def template = [path: path]
        def override = groovyPageLocator.findTemplateInBinding(path, pageScope)
        if (!override) {
            template.plugin = 'filterpane'
        }
        template
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

        if (attrs.total != null) {
            count = attrs.total
        } else if (attrs.domainBean) {
            def dc = FilterPaneUtils.resolveDomainClass(grailsApplication, attrs.domainBean)

            if (dc) {
                count = dc.clazz.count()
            }
        }
        attrs.total = count
        attrs.params = filterParams
        out << g.paginate(attrs, body)
    }

    def currentCriteria = { attrs, body ->
        def renderModel = [:]
        boolean useFullAssociationPath = resolveBoolAttrValue(attrs.fullAssociationPathFieldNames ?: 'y')
        renderModel.isFiltered = FilterPaneUtils.isFilterApplied(params)
        if (renderModel.isFiltered == true) {
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
                if (key.startsWith('filter.op.') && filterOp != null && filterOp != '') {
                    return key[10..-1]
                }
                false
            }

            def getDomainProp = { prop ->
                if (prop.contains('.')) { // association.
                    def parts = prop.toString().split('\\.')
                    def domainProp
                    def domainObj = domainBean
                    int lastPartIndex = parts.size() - 1
                    for (int i = 0; i < lastPartIndex; i++) {
                        domainObj = domainObj.getPropertyByName(parts[i]).referencedDomainClass
                    }

                    return domainObj.getPropertyByName(parts[lastPartIndex])
                }

                prop != 'class' ? domainBean.getPropertyByName(prop) : null;
            }

            //log.debug("=================================================================")
            //log.debug("current criteria filterParams: ${filterParams}")

            filterParams.each { key, filterOp ->

                def criteriaModel = [:]
                def prop = getProp(key, filterOp)

                if (prop) {
                    def domainProp = getDomainProp(prop)
                    def filterValue = filterParams["filter.${prop}"]
                    def filterValueTo
                    boolean isNumericType = (domainProp?.referencedPropertyType
                            ? Number.isAssignableFrom(domainProp?.referencedPropertyType)
                            : false)
                    boolean isNumericAndBlank = isNumericType && !"".equals(filterValue.toString().trim())
                    boolean isDateType = (domainProp?.referencedPropertyType
                            ? FilterPaneUtils.isDateType(domainProp?.referencedPropertyType)
                            : false)
                    boolean isEnumType = domainProp?.referencedPropertyType?.isEnum()
                    if (filterValue != null && (!isNumericType || isNumericAndBlank) && filterOp?.size() > 0) {

                        if (isDateType) {
                            def clazz = domainProp?.type ?: domainProp?.class
                            filterValue = FilterPaneUtils.parseDateFromDatePickerParams("filter.${prop}", filterParams, clazz)
                            if (filterValue) {
                                def df = renderModel.dateFormat
                                if (df instanceof Map) {
                                    df = renderModel.dateFormat[prop]
                                }
                                if (AbstractPartial.isAssignableFrom(clazz) || AbstractInstant.isAssignableFrom(clazz)) {
                                    filterValue = joda.format(value: filterValue, pattern: df)
                                } else {
                                    filterValue = g.formatDate(format: df, date: filterValue)
                                }
                            }
                        } else if (isEnumType) {
                            def tempMap = [:]

                            addFilterPropertyValues(attrs, tempMap, prop)

                            if (filterValue.class.isArray()) {
                                filterValue = filterValue.collect {
                                    Enum.valueOf(domainProp?.referencedPropertyType, it.toString())
                                }.join(', ')
                            } else {
                                def enumValue = Enum.valueOf(domainProp?.referencedPropertyType, filterValue.toString())
                                if (enumValue && tempMap.displayProperty) {
                                    filterValue = tempMap.displayProperty == 'name' ? enumValue.name() : enumValue[tempMap.displayProperty]
                                }
                            }

                        }

                        def lcFilterOp = filterOp.toString().toLowerCase()
                        switch (lcFilterOp) {

                            case FilterPaneOperationType.IsNull.operation.toLowerCase():
                            case FilterPaneOperationType.IsNotNull.operation.toLowerCase():
                                filterValue = ''
                                break
                            case FilterPaneOperationType.Between.operation.toLowerCase():
                                filterValueTo = filterParams["filter.${prop}To"]
                                if (filterValueTo == 'date.struct' || filterValueTo == 'struct') {
                                    filterValueTo = FilterPaneUtils.parseDateFromDatePickerParams("filter.${prop}To", params)
                                    if (filterValueTo) {
                                        def df = renderModel.dateFormat
                                        if (df instanceof Map) {
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
                        criteriaModel.domainProp = domainProp
                        // <-- TODO: look at this and resolve it how it is done on filterPane tag.
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
            Map template = getTemplatePath('currentCriteria')

            out << g.render(template: template.path, plugin: template.plugin, model: renderModel)
        }

    }

    def filterPane = { attrs, body ->

        if (!attrs.domain) {
            log.error("domain attribute is required")
            return
        }

        def renderModel = [customForm: false]

        // Validate required info
        def domain = FilterPaneUtils.resolveDomainClass(grailsApplication, attrs.domain)
        if (domain == null) {
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
                renderModel.action
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
        List persistentProps = (domain.persistentProperties + domain.associations).unique() as List
        List additionalPropNames = resolveListAttribute(attrs.additionalProperties)
        List excludePropNames = resolveListAttribute(attrs.excludeProperties)
        List associatedPropNames = resolveListAttribute(attrs.associatedProperties)

        // DEPRECATION: attribute name filterProperties as of 2.0
        List explicitPropNames = resolveListAttribute(attrs.explicitProperties ?: attrs.filterProperties)

        // If they explicitly requested certain props, remove the others.
        if (explicitPropNames.size() > 0) {
            persistentProps = persistentProps.findAll {
                explicitPropNames.contains(it.name)
            }
        }

        // Extract out the associations.  These are handled separately from simple properties.
        List associatedProps = persistentProps.findAll {
            it instanceof Association && !it.type.isEnum()
        }
        persistentProps.removeAll(associatedProps)


        def lastUpdatedProp = persistentProps.find {
            it.name == LAST_UPDATED
        }

        if (lastUpdatedProp != null) {

            // Verify they did not explicitly request it before removing it.
            boolean keep = explicitPropNames.contains(LAST_UPDATED) || additionalPropNames.contains(LAST_UPDATED)
            if (!keep) {
                persistentProps.remove(lastUpdatedProp)
            }
        }

        // Resolve additional properties: id, version and sub class attributes.
        def subClassPersistentProps = FilterPaneUtils.resolveSubDomainsProperties(domain)
        for (ap in additionalPropNames) {
            if ("id".equals(ap) || "identifier".equals(ap)) {
                finalProps[domain.identifier.name] = domain.identifier
            } else if ("version".equals(ap)) {
                finalProps[domain.version.name] = domain.version
            } else {
                def subClassProperty = subClassPersistentProps.find { it.name == ap }
                if (subClassProperty)
                    finalProps[subClassProperty.name] = subClassProperty
            }
        }

        excludePropNames.each { name ->
            def epObj = persistentProps.find {
                it.name == name
            }
            if (epObj != null) {
                log.debug "Removing ${name} from final props"
                persistentProps.remove(epObj)
            }
        }

        // Construct the final properties to filter
        persistentProps.each { finalProps[it.name] = it }

        // Add the associated properties
        associatedPropNames.each { dottedName ->
            addAssociatedProperty(finalProps, dottedName, associatedProps)
        }

        log.debug "${finalProps.size()} final props: ${finalProps}"

        // sortedProps is a list of Entry instances where the key is the property name and the value is a GrailsDomainClassProperty instance.
        // The list is sorted by order the properties appear in the GrailsDomainClass

//        Class clz = new GrailsAwareClassLoader().loadClass('org.grails.validation.DomainClassPropertyComparator')
//        clz = clz ?: new GrailsAwareClassLoader().loadClass('org.grails.validation.DomainClassPropertyComparator')
//        def constructor = clz.getConstructor(GrailsDomainClass)
//        def domainComparator = constructor.newInstance(domain)

//        def domainComparator = Class.forName('org.grails.validation.DomainClassPropertyComparator')
//        if(!domainComparator){
//            domainComparator = Class.forName('org.grails.validation.DomainClassPropertyComparator')
//        }
//        domainComparator.newInstance(domain)
//        def domainComparator = new org.grails.validation.DomainClassPropertyComparator(domain)

        // TODO - just sort by keys for now
        def sortedProps = finalProps.entrySet().asList().sort { a, b -> a.key <=> b.key }

        // add 'class' property if domain class has its implementers
        boolean hasSubClasses = !grailsApplication.mappingContext.getChildEntities(domain).isEmpty()
        if (hasSubClasses && !excludePropNames.contains("class")) {
            // class property should be as a first in a sorted props
            sortedProps.add(0, new MapEntry("class", [name: "class", type: Class, domainClass: domain, naturalName: "Class"]))
            // fake GrailsDomainClassProperty object
            log.debug "Add 'class' property to sortedProps"
        }

        renderModel.properties = []

        mapSortedProps(sortedProps, finalProps, attrs, useFullAssociationPath, renderModel)

        def sortKeys = sortedProps.collect { it.key }
        log.debug("Sorted props: ${sortedProps}")
        log.debug("Sort keys: ${sortKeys}")

        assignRenderModels(attrs, sortedProps, sortKeys, renderModel)

        Map template = getTemplatePath('filterpane')

        out << g.render(template: template.path, plugin: template.plugin, model: [fp: renderModel])
    }

    def date = { attrs, body ->

        def domainClass = FilterPaneUtils.resolveDomainClass(grailsApplication, attrs.domain)
        def domainProperty = FilterPaneUtils.resolveDomainProperty(domainClass, attrs.propertyName)
        def d = FilterPaneUtils.parseDateFromDatePickerParams(attrs.name, params, domainProperty.type)
        def model = [:]
        model.putAll(attrs)
        model.value = d
        model.onChange = "grailsFilterPane.selectDefaultOperator('${attrs.opName}')"
        model.isDayPrecision = (attrs.precision == 'day') ? 'y' : 'n'
        model.domainProperty = domainProperty


        Map template = getTemplatePath('dateControl')

        out << g.render(template: template.path, plugin: template.plugin, model: [ctrlAttrs: model])
    }

    def datePicker = { attrs, body ->
        def ctrlAttrs = attrs.ctrlAttrs
        def type = ctrlAttrs.domainProperty.type

        if (Date.isAssignableFrom(type)) {
            out << g.datePicker(ctrlAttrs)
        } else if (DateTime.isAssignableFrom(type) || Instant.isAssignableFrom(type) || LocalDateTime.isAssignableFrom(type)) {
            out << joda.dateTimePicker(ctrlAttrs)
        } else if (LocalTime.isAssignableFrom(type)) {
            out << joda.timePicker(ctrlAttrs)
        } else if (LocalDate.isAssignableFrom(type)) {
            out << joda.datePicker(ctrlAttrs)
        }
    }

    def bool = { attrs, body ->
        Map template = getTemplatePath('boolean')

        out << g.render(template: template.path, plugin: template.plugin, model: attrs)
    }

    def input = { attrs, body ->

        def ret

        if (attrs?.ctrlType) {
            switch (attrs.ctrlType) {
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
                case 'select-list':
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
     * @param sort
     *          The attribute to sort upon after filtering
     * @param order
     *          The sort order to use after filtering
     * @param *
     *          Additionally you may use all the optional parameters/attrs that you can use to the tag g:link.
     * @body The body of this tag should contain the text to display within the link.
     */
    def filterLink = { attrs, body ->
        def filterParams = attrs.filterParams
        def values = attrs.values
        def label = body()
        def controller = attrs.controller
        def action = attrs.action ?: 'filter'
        // Only copy default search params if it is the same controller
        def sort = attrs.sort ?: (controller == "$controllerName" ? params.sort : null)
        def order = attrs.sort ?: (controller == "$controllerName" ? params.order : null)

        def linkParams = [:]
        if (filterParams) {
            linkParams.putAll(filterParams)
        }
        if (!values) {
            throw new IllegalArgumentException("Mandatory argument 'values' is missing.")
        }
        if (!values instanceof Map) {
            throw new IllegalArgumentException("Mandatory argument 'values' needs to be of type Map.")
        }
        linkParams.sort = sort
        linkParams.order = order
        for (String field : values.keySet()) {
            def value = values[field]

            if (value instanceof Map && value?.op && !FilterPaneOperationType.getFilterPaneOperationType(value.op)) {
                throw new RuntimeException("Operation type ${value.op} is not supported.  Please see FilterPaneOperationType for supported operations.")
            }

            if (value == null || value == 'null') {
                linkParams['filter.op.' + field] = FilterPaneOperationType.IsNull.operation
                linkParams['filter.' + field] = '0'
            } else if (value instanceof Map) {
                if (value.op == FilterPaneOperationType.IsNull.operation || value.op == FilterPaneOperationType.IsNotNull.operation) {
                    value.value = '0'
                }
                linkParams['filter.op.' + field] = value.op ?: FilterPaneOperationType.Equal.operation
                linkParams['filter.' + field] = value.value ?: value.from
                if (value.to) {
                    linkParams["filter.${field}To"] = value.to
                }

            } else {
                linkParams['filter.op.' + field] = FilterPaneOperationType.Equal.operation
                // Find the value also for referenced child objects
                linkParams['filter.' + field] = value
            }
        }

        def linkAttrs = [action: action]
        linkAttrs.putAll(attrs)
        linkAttrs.remove('values')
        linkAttrs.remove('filterParams')
        linkAttrs.params = linkParams

        out << g.link(linkAttrs) { label }
    }

    private void assignRenderModels(Map attrs, List sortedProps, ArrayList sortKeys, LinkedHashMap<String, Boolean> renderModel) {
        //noinspection GroovyAssignabilityCheck
        renderModel.sortModel = [sortValueMessagePrefix: attrs.sortValueMessagePrefix ?: null,
                                 sortedProperties      : sortedProps.collect { it.value },
                                 sortKeys              : sortKeys,
                                 sortValue             : params.sort ?: "",
                                 noSelection           : ['': g.message(code: 'fp.tag.filterPane.sort.noSelection.text', default: 'Select a Property')],
                                 orderAsc              : params.order == 'asc',
                                 orderDesc             : params.order == 'desc']

        //noinspection GroovyAssignabilityCheck
        renderModel.buttonModel = [
                cancelText : g.message(code: 'fp.tag.filterPane.button.cancel.text', default: 'Cancel'),
                clearText  : g.message(code: 'fp.tag.filterPane.button.clear.text', default: 'Clear'),
                applyText  : g.message(code: 'fp.tag.filterPane.button.apply.text', default: 'Apply'),
                action     : renderModel.action,
                containerId: renderModel.containerId,
                formName   : renderModel.formName]
    }

    private void mapSortedProps(List sortedProps, finalProps, attrs, boolean useFullAssociationPath, renderModel) {
        sortedProps.each { entry ->
            def propertyKey = entry.key
            def sp = entry.value
            def map = [domainProperty: sp]
            def opName = "filter.op.${propertyKey}"
            def propertyType = sp.type
            def type = FilterPaneUtils.getOperatorMapKey(propertyType)


            def name = "filter.${propertyKey}"
            def domain = sp.getOwner().getJavaClass().name
            map.ctrlAttrs = [name: name, value: params[name], opName: opName, domain: domain, propertyName: sp.name]//, domainProperty:sp]
            addFilterPropertyValues(attrs, map.ctrlAttrs, propertyKey)

            def opKeys = []
            opKeys.addAll(availableOpsByType[type])

            // If the property is not nullable, no need to allow them to filter
            // in is or is not null.
            def constrainedProperty = grailsApplication.mappingContext.getEntityValidator(sp.getOwner())?.getConstrainedProperties()?.get(sp.name)
//            def constrainedProperty = sp.domainClass.constrainedProperties[sp.name]
            if ((constrainedProperty && !constrainedProperty.isNullable()) || sp.name == 'id') {
                opKeys.remove(FilterPaneOperationType.IsNotNull.operation)
                opKeys.remove(FilterPaneOperationType.IsNull.operation)
            }

            map.ctrlType = "text"
            if (FilterPaneUtils.isDateType(propertyType)) {
                map.ctrlType = "date"
            } else if (propertyType == Boolean.class || propertyType == boolean.class) {
                map.ctrlType = "boolean"
            }

            // If the user did not specify a value list and the property is
            // constrained with one, use the domain class's list.
            if (!map.ctrlAttrs.values) {
                List inList = constrainedProperty?.getInList()
                if (inList) {
                    map.ctrlAttrs.values = inList
                } else if (type == 'class') { // property is class type
                    if (sp.name == 'class') { // class attribute for inheritance
                        def domainClasses = grailsApplication.mappingContext.getChildEntities(sp.getOwner()).findAll {
                            !Modifier.isAbstract(it.getJavaClass().modifiers)
                        } // do not add abstract classes
                        map.ctrlAttrs.values = domainClasses.collect { it.getJavaClass().simpleName } // set values
                        map.ctrlAttrs.keys = domainClasses.collect { it.name } // set keys
                    } else { // custom class attribute
                        def classes = sp.getOwner().getJavaClass().createCriteria().listDistinct {
                            projections {
                                distinct(sp.name)
                            }
                        }
                        map.ctrlAttrs.values = classes.collect { it.simpleName } // set values
                        map.ctrlAttrs.keys = classes.collect { it.name } // set keys
                    }
                } else if (sp.type.isEnum()) {
                    //map.ctrlAttrs.values = sp.type.enumConstants as List
                    def valueList = []

                    sp.type.enumConstants.each {
                        def value = it
                        if (map.ctrlAttrs.valueProperty) {
                            value = map.ctrlAttrs.valueProperty == 'ordinal' ? it.ordinal() : it[map.ctrlAttrs.valueProperty]
                        }
                        def display = value
                        if (map.ctrlAttrs.displayProperty) {
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
            if (map.ctrlAttrs.values) {
                opKeys = ['', FilterPaneOperationType.Equal.operation, FilterPaneOperationType.NotEqual.operation]

                // Also set up the rest of the dropdown ctrl attrs
                map.ctrlAttrs.from = map.ctrlAttrs.values
                map.ctrlAttrs.remove('values') // transferred to "from" property
                map.ctrlAttrs.noSelection = ['': '']

                def valueMessagePrefix = map.ctrlAttrs.valueMessagePrefix ?: "fp.property.text.${sp.name}"
                def valueMessageAltPrefix = "${sp.getOwner().getDecapitalizedName()}.${sp.name}"
                def messageSource = grailsAttributes.getApplicationContext().getBean("messageSource")
                def locale = RequestContextUtils.getLocale(request)
                if (messageSource.getMessage(valueMessagePrefix, null, null, locale) != null) {
                    map.ctrlAttrs.valueMessagePrefix = valueMessagePrefix
                } else if (messageSource.getMessage(valueMessageAltPrefix, null, null, locale) != null) {
                    map.ctrlAttrs.valueMessagePrefix = valueMessageAltPrefix
                }
                map.ctrlType = "select"

                if (map.domainProperty.type.isEnum()) {
                    if (map.ctrlAttrs?.valueMessagePrefix) {
                        map.ctrlAttrs.remove('optionValue')
                    }
                    opKeys = ['', FilterPaneOperationType.InList.operation, FilterPaneOperationType.NotInList.operation]
                    def tempVal = map.ctrlAttrs.value
                    def newValue = null
                    try {
                        if (tempVal instanceof Object[]) {
                            newValue = tempVal.collect { Enum.valueOf(map.domainProperty.type, it.toString()) }
                        } else if (tempVal?.toString()?.length() > 0) {
                            newValue = Enum.valueOf(map.domainProperty.type, tempVal.toString())
                        }
                    } catch (IllegalArgumentException iae) {
                        log.debug("Enum valueOf failed. value is ${tempVal}", iae)
                        // Ignore this.  val is not a valid enum value (probably an empty string).
                    }
                    map.ctrlAttrs.value = newValue
                    map.ctrlAttrs.multiple = true
                    map.ctrlType = "select-list"
                }

                if (type == 'class') {
                    opKeys = ['', FilterPaneOperationType.InList.operation, FilterPaneOperationType.NotInList.operation]
                    def tempVal = map.ctrlAttrs.value
                    def newValue
                    newValue = null // default to null.  If it's valid, it'll get replaced with the real value.
                    if (tempVal instanceof Object[]) {
                        newValue = tempVal.collect { it.toString() }
                    } else if (tempVal.toString().length() > 0) {
                        newValue = tempVal.toString()
                    }
                    map.ctrlAttrs.value = newValue
                    map.ctrlAttrs.multiple = true
                    map.ctrlType = "select-list"
                }
            }

            if (map.ctrlType == 'select' || map.ctrlType == 'select-list' || map.ctrlType == 'text') {
                map.ctrlAttrs.onChange = "grailsFilterPane.selectDefaultOperator('${opName}')"
            }

            // Create the operator dropdown attributes.
            map.opName = opName
            map.opKeys = opKeys
            map.opValue = params[opName]
            if (params[opName] == FilterPaneOperationType.IsNull.operation || params[opName] == "IsNotNull") {
                map.ctrlAttrs.style = 'display:none;'
            }

            // Note: propertyKey is the dotted name
            def fieldName = resolveFieldName(propertyKey, sp, propertyKey.contains('.'), useFullAssociationPath)

            map.fieldLabel = fieldName

            // Add this new field name as a property of this instance
            if (sp.metaClass)
                sp.metaClass.getFilterPaneFieldName = { -> new String(fieldName) }
            else
                sp.getFilterPaneFieldName = { -> new String(fieldName) }

            // For numeric and date types, build the "To" control, in case they select between.
            if (type == "numeric" || type == "date") {
                map.toCtrlAttrs = [:]
                map.toCtrlAttrs.putAll(map.ctrlAttrs)
                map.toCtrlAttrs.name += "To"
                map.toCtrlAttrs.id += "To"
                map.toCtrlAttrs.value = params[map.toCtrlAttrs.name]

                boolean showToCtrl = "between".equalsIgnoreCase(params[opName])
                if (showToCtrl) {
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
    private resolveAttribute(String customKey, String localizationKey, attrValue, String defaultValue) {
        def result

        if (customKey) {
            result = g.message(code: customKey, default: defaultValue)
        } else {
            result = attrValue ?: g.message(code: localizationKey, default: defaultValue)
        }

        result ?: defaultValue
    }

    private List resolveListAttribute(attr) {
        List temp = []
        if (attr != null) {
            if (attr instanceof List) {
                temp = attr
            } else if (attr instanceof String) {
                temp = attr.split(",") as List
            }
        }
        temp.collect { it.trim() }
    }

    private void addAssociatedProperty(finalProps, String dottedName, List associatedProps) {

        List parts = dottedName.split('\\.')
        def association = associatedProps.find { it.name == parts[0] }
        def refDomain
        def refProperty
        int index = 1
        def fieldNamePrefix = ""

        while (association && index < parts.size()) {
            refDomain = FilterPaneUtils.resolveReferencedDomainClass(association)
            fieldNamePrefix += "${grails.util.GrailsNameUtils.getNaturalName(refDomain.getJavaClass().simpleName)}'s "
            refProperty = ("id".equalsIgnoreCase(parts[index]) || "identifier".equalsIgnoreCase(parts[index])) ?
                    refDomain.identity :
                    refDomain.persistentProperties.find { it.name == parts[index] }
            //log.debug("refDomain is ${refDomain}, refProperty is ${refProperty}, parts[${index}] = ${parts[index]}")
            association = (refProperty instanceof Association && refProperty?.type?.isEnum() == false) ? refProperty : null
            index += 1
        }

        // search for refProperty in sub classes
        if (!refProperty && refDomain) {
            def subClassPersistentProps = FilterPaneUtils.resolveSubDomainsProperties(refDomain)
            refProperty = subClassPersistentProps.find { it.name == parts[parts.size() - 1] } // last attribute matter
        }

        if (refProperty && !(refProperty instanceof Association)) {
            log.debug("adding association ${dottedName}")
            def prefixMethod = "getPrefix${dottedName.replaceAll('\\.', '')}"
            refProperty.metaClass."${prefixMethod}" = { -> fieldNamePrefix }
            finalProps[dottedName] = refProperty
        }
    }

    private addFilterPropertyValues(tagAttrs, ctrlAttrs, propertyKey) {
        if (tagAttrs.filterPropertyValues && tagAttrs.filterPropertyValues[propertyKey]) {
            ctrlAttrs.putAll(tagAttrs.filterPropertyValues[propertyKey])
        }

        if (!ctrlAttrs.id) {
            ctrlAttrs.id = propertyKey
        }
    }

    private Boolean resolveBoolAttrValue(String attr) {
        ['y', 't', 'yes', 'true'].contains(attr?.toLowerCase())
    }

    private String resolveFieldName(propName, sp, boolean isAssociation, boolean useFullAssociationPath) {
        // Take care of the name (label).  Yuck!
        def fieldNameKey = "fp.property.text.${propName}" // Default.
        def fieldNameAltKey = fieldNameKey // default for alt key.
        def className = StringUtils.uncapitalize(sp?.getOwner()?.getJavaClass()?.simpleName)
        def fieldNamei18NTemplateKey = "${className}.${sp?.name}"
        def fieldName = GrailsNameUtils.getNaturalName(sp?.name)

        if (isAssociation) { // association.
            fieldNameKey = "fp.property.text.${sp?.getOwner()?.getJavaClass()?.simpleName}.${sp?.name}"
            fieldNamei18NTemplateKey = "${sp?.getOwner()?.getDecapitalizedName()}.${sp?.name}"
            // GRAILSPLUGINS-2027 Fix.  associated properties displaying package name.
            def prefix = ""
            def prefixMethod = "prefix${propName.replaceAll('\\.', '')}"
            if (sp?."${prefixMethod}" && useFullAssociationPath) {
                prefix = sp."${prefixMethod}"
            } else {
                prefix = "${grails.util.GrailsNameUtils.getNaturalName(sp?.getOwner()?.getJavaClass()?.simpleName)}'s "
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
