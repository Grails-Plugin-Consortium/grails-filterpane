Filterpane Plugin
=================

Build Status

[![Build Status](https://travis-ci.org/Grails-Plugin-Consortium/grails-filterpane.png?branch=master)](https://travis-ci.org/Grails-Plugin-Consortium/grails-filterpane)

Demo project located at [grails-filterpane-test](https://github.com/Grails-Plugin-Consortium/grails-filterpane-test)

# FilterPane Plugin #
Help Grails be an active, open community!  If you use this plugin and haven't rated it, please do so.  If you've rated this plugin anything but 5 stars, please let me know why (See [#Support] for how).  I'm striving to make this plugin the best that it can be, and constructive feedback is always appreciated.

## Upgrading to 2.0. ##
Version 2.0 is a complete rewrite / refactoring of the plugin to make better use of modern Grails practices (and a better working knowledge of Grails).  All tags are now rendered via templates instead of string builders in code.  This should make the plugin more extensible in the future.  Many bugs and feature enhancements were also completed for the release.

### Release Notes - Grails Plugins - Version Grails-FilterPane 2.0 ###
#### Bug ####
* [GRAILSPLUGINS-1256](http://jira.codehaus.org/browse/GRAILSPLUGINS-1256) - plugin overrides default order of domain objects
* [GRAILSPLUGINS-1717](http://jira.codehaus.org/browse/GRAILSPLUGINS-1717) - Filterpane "java.lang.ClassCastException: java.lang.String" with Long and Integer properties
* [GRAILSPLUGINS-2446](http://jira.codehaus.org/browse/GRAILSPLUGINS-2446) - i18n missing for sortable criteria combobox
* [GRAILSPLUGINS-2447](http://jira.codehaus.org/browse/GRAILSPLUGINS-2447) - Missing input field for associated properties if between criteria is selected
* [GRAILSPLUGINS-2483](http://jira.codehaus.org/browse/GRAILSPLUGINS-2483) - Missing input field for type Currency
* [GRAILSPLUGINS-2807](http://jira.codehaus.org/browse/GRAILSPLUGINS-2807) - GSP-tag filterpane:includes generates wrong path for java-script files.
* [GRAILSPLUGINS-2808](http://jira.codehaus.org/browse/GRAILSPLUGINS-2808) - Associated properties not displayed correctly in currentCriteria tag
* [GRAILSPLUGINS-2813](http://jira.codehaus.org/browse/GRAILSPLUGINS-2813) - The currentCriteria tag looses the sort and order params when using remove link

#### Improvement ####
* [GRAILSPLUGINS-1476](http://jira.codehaus.org/browse/GRAILSPLUGINS-1476) - Support filtering of collections
* [GRAILSPLUGINS-1979](http://jira.codehaus.org/browse/GRAILSPLUGINS-1979) - Specify fetchMode In FilterPane
* [GRAILSPLUGINS-2448](http://jira.codehaus.org/browse/GRAILSPLUGINS-2448) - German properties
* [GRAILSPLUGINS-2802](http://jira.codehaus.org/browse/GRAILSPLUGINS-2802) - Swedish messages
* [GRAILSPLUGINS-2809](http://jira.codehaus.org/browse/GRAILSPLUGINS-2809) - Make it possible to filter on "id" property

#### New Feature ####
    * [GRAILSPLUGINS-1909](http://jira.codehaus.org/browse/GRAILSPLUGINS-1909) - Support for deeper assocations

There are several breaking changes when migrating to 2.0.  Please take note!
* Package names for all classes have changed to a more standard one: org.grails.plugin.filterpane
* Class Renames
* FilterService has been renamed to FilterPaneService to match the plugin name and to remove ambiguity with other plugins / services
* FilterUtils has been renamed to FilterPaneUtils for the same reasons as above.
* Tag Library Changes
* filePaneIncludes tag has been removed.  It has been deprecated for some time.  Use includes tag instead.
* filterButton no longer uses an ID attribute, as it was unclear what it was for.  It has been renamed to filterPaneId, to denote the id of the filterpane you wish to display.
* filterPane tag domainBean attribute is now just "domain"
* A couple years of practical use has shown that most users are NOT using the filter form as a dialog.  As such, a new required attribute to the filterPane tag has been added: dialog.  It defaults to false, so if you want to maintain the use of the form as a popup dialog, you must add this attribute and set it to true.
This plugin adds filtering capabilities to any Grails application. The primary goals of this plugin include:
* Easy integration with Grails list pages via custom tags
* Smart operator display. Only practical filter operations are available for a property's data type. (e.g. no "like" operators for numeric properties)
* Support for major operators including =, <>, >, <, >=, <=, like, ilike, between, is null, and is not null.
* Smart filter value entry. Date properties display a date picker, boolean's display radio buttons, etc.
* Support for a custom list of values, such as a filtering a text property with a constrained list of values.
* Works with Grails list sorting out of the box with only minor modification to your list gsp.
* Works with Grails pagination out of the box with only minor modification to your list gsp.
* Honors domain constraints: nullable, blank, inList ( _Since 0.4_ )
Please note that you may experience duplicate rows if you use eager fetching.  See [http://jira.codehaus.org/browse/GRAILSPLUGINS-2063] for more information.
The new demo application can be downloaded here: [https://github.com/Grails-Plugin-Consortium/grails-filterpane-test]

##  Usage ##
The plugin is typically used in an application's list pages.  To use the filters, make the following changes to your list.gsp pages.  For a full list of tags and attributes, see the Reference section later in this document.

### list.gsp ###
1.  Add the javascript and stylesheet includes to the head section of your page.

```
<filterpane:includes />
```

2.  Somewhere in your page (typically near the bottom of the body tag), add the filter pane.  This has the result of rendering a container div in your page. The domain is the fully qualified domain class name (domainClass.fullName)

```
<filterpane:filterPane domain="MyDomainClass" />
```

3.  Add a button to invoke the filter pane somewhere on your list page. (Inside the pagination div works well on applications built from scaffolding.)
A custom tag is provided to help create this button. The title attribute is optional. If omitted, the value of the button will be "Filter".

```
<filterpane:filterButton text="Whatever You Wish" />
```

If your application uses scriptaculous and you use the filterButton tag, the filter pane will fade in and out. Otherwise the filter pane simply appears and disappears.
#### Pagination ####
If you want to support pagination use something similar to the following:
```
<g:paginate total="${bookCount == null ? Book.count(): bookCount}" params="${filterParams}" />
```
In the example above, _bookCount_ is any variable you pass into the controller's render model that contains the total number of records returned in your filtered data.  Its value can be obtained from the filter service's _count_ method.  _(See the controller section later in this document for more info.)_
{warning}
Note that you should not use Groovy's Elvis operator to test for bookCount's existence, as an empty set (count of 0) will return false, thus causing Book.count() to be used.  See "Groovy Truth":http://groovy.codehaus.org/Groovy+Truth for more info.
{warning}

Also in the example above, filterParams is set in the model map of the render call in the controller.  The value is a sub-map of the request params, and can be obtained by calling the "extractFilterParams" method on the included FilterUtils class.  See the example below from the books example app.
```
render( view:'list',
    model:[ bookList: filterPaneService.filter( params, Book ),
        bookCount: filterPaneService.count( params, Book ),
        filterParams: org.grails.plugin.filterpane.FilterPaneUtils.extractFilterParams(params),
        params:params ] )
```

Starting with version 0.6 of the plugin, you can use the new paginate tag.  The paginate tag wraps the Grails paginate tag and encapsulates the steps listed above.  See the [#paginate] tag for more info.

#### List Sorting ####
If you want to support Grails's list sorting, you must add a the filter parameters to each Grails sortableColumn tag, as shown in the example below.  It is not recommended to put the entire "params" map in here.  Instead, it is recommended that you only include the FilterPane parameters, which is the same sub-map as is described in the Pagination section above.
```
<g:sortableColumn property="id" title="Id" params="${filterParams}" />
```

#### Controller ####
1.  Inject the filter service into your controller.

```
def filterPaneService
```

2.  Create the filter action in your controller.

```
def filter = {
    if(!params.max) params.max = 10
    render( view:'list',
        model:[ domainClassList: filterPaneService.filter( params, DomainClass ),
        domainClassCount: filterPaneService.count( params, DomainClass ),
        filterParams: org.grails.plugin.filterpane.FilterPaneUtils.extractFilterParams(params),
        params:params ] )
}
```

Where...
* domainClassList is the name of the return list model the list action uses
* DomainClass is the name of the domain class you are filtering
* domainClassCount is the name of a variable that contains the total number of filtered results.  This parameter is optional, but is useful if you want to use Grails pagination (which most of the time you will).
* filterParams is a sub-map of the request parameters obtained via the FilterUtils.extractFilterParams method.
Keep in mind that you don't have to name the action "filter".  You can name it anything you want, just remember to assign the same name to the filterPane tag's @action@ attribute so your action gets called when applying the filter.

## Plugin Version History ##
<table>
<tr><td>*Date* </td><td> *Version* </td><td> *Notes* </td><td> *Known Compatible Grails Versions*</td></tr>
<tr><td>2013-04-12 </td><td> 2.1 </td><td> Addressing several JIRAs.  Templates now copied to project allowing for overridding.  Get working with Grails 2.2.x.</td><td>2.0</td></tr>
<tr><td>2011-03-19 </td><td> 2.0 </td><td> Complete rewrite of the plugin  See JIRA notes for changes.</td><td></td></tr>
<tr><td>2010-07-26 </td><td> 0.7 </td><td> Bug fixes. </td><td> 1.1.1 +</td></tr>
<tr><td>2010-03-24 </td><td> 0.6.8 </td><td> Bug fix for boolean 'false' value not working </td><td> 1.1.1 +</td></tr>
<tr><td>2010-03-23 </td><td> 0.6.7 </td><td> Some bug fixes and improvements to Grails-1.2.1 compatibility </td><td> 1.1.1 +</td></tr>
<tr><td>2010-02-17 </td><td> 0.6.6 </td><td> Should now work with Grails-1.2.1 and 1.1.1 </td><td> 1.1.1 +</td></tr>
<tr><td>2010-02-03 </td><td> 0.6.5 </td><td> Fixed several issues.  See (http://jira.codehaus.org/browse/GRAILSPLUGINS/fixforversion/16016) </td><td> 1.1.1 +</td></tr>
<tr><td>2009-11-17 </td><td> 0.6.4 </td><td> Added customForm attribute to filterPane tag.  See docs for details in the reference section below. </td><td> 1.1.1</td></tr>
<tr><td>2009-10-27 </td><td> 0.6.3 </td><td> Fixed (http://jira.codehaus.org/browse/GRAILSPLUGINS-1629) </td><td> 1.1.1</td></tr>
<tr><td>2009-09-13 </td><td> 0.6.2 </td><td> Child collection filtering fixed (GRAILSPLUGINS-1503).  Filter service reworked to use Grails API instead of Groovy meta classes.  Several integration tests added. </td><td> 1.1.1</td></tr>
<tr><td>2009-07-28 </td><td> 0.6.1 </td><td> Fixed a minor issue when rendering dropdowns for enums in associated properties. </td><td> 1.1.1</td></tr>
<tr><td>2009-07-27 </td><td> 0.6  </td><td> Completed several JIRA issues.  See (http://jira.codehaus.org/browse/GRAILSPLUGINS/fixforversion/15149) for details. </td><td> 1.1</td></tr>
<tr><td>2009-04-07 </td><td> 0.5  </td><td> Fixed JIRA issues 836, 988, and 1045.  Form action is now POST.  Entering in a filter value will now auto-select the first operator in its associated dropdown if none is selected (except for date properties).  More info on issues at (http://jira.codehaus.org/browse/GRAILSPLUGINS/fixforversion/15114) </td><td> 1.1</td></tr>
<tr><td>2009-03-22 </td><td> 0.4.3  </td><td> JIRA GRAILSPLUGINS-985 fixed. </td><td> 1.1</td></tr>
<tr><td>2009-03-22 </td><td> 0.4.2  </td><td> Now compatible with Grails 1.1.  See JIRA GRAILSPLUGINS-999 </td><td> 1.0.4, 1.1</td></tr>
<tr><td>2009-03-22 </td><td> 0.4.1  </td><td> Completed JIRAs GRAILSPLUGINS-822 and GRAILSPLUGINS-903 </td><td> 1.0.4</td></tr>
<tr><td>2009-01-27 </td><td> 0.3.1  </td><td> Moved debug output to log.debug statements. </td><td> 1.0.4</td></tr>
<tr><td>2009-01-26 </td><td> 0.3  </td><td> Fixed packaging glitches.  Updated example app. </td><td> 1.0.4</td></tr>
<tr><td>2009-01-25 </td><td> 0.2  </td><td> No major changes.  Mainly code cleanup.  Classes are now in packages, as per plugin specs. </td><td> 1.0.4</td></tr>
<tr><td>2009-01-16 </td><td> 0.1  </td><td> First release. </td><td> 1.0.4</td></tr>
</table>

## Roadmap ##
*  Minor versions of 0.6 will address issues that arise with 0.6 and any critical bugs that are found.
*  Revised documentation that is more user friendly and improved examples of each feature.
*  Release 0.7 will focus on adding more unit and functional tests and simplifying the code base.
*  Future releases will be kept smaller than 0.5 and 0.6 in an attempt to deliver changes faster.

## Support ##
* For support questions, please use the Grails user mailing list (user@grails.codehaus.org), and include the word "filterpane" somewhere in your message.
* JIRA issues may be found [here](http://jira.grails.org/browse/GPFILTERPANE).

## Reference ##
The reference section is only applicable to versions 0.4 and above.  Version 0.4 addressed some redundancy in the tag and attribute names provided by the plugin.  While all previous attribute names are still supported, it is recommended to use those documented below instead.  The deprecated tag and attribute names may be removed in the future.

### Data Types and their Available Filter Operators ###

#### Text ####
_(String, char)_
<table>
<tr><td>*Operator* </td><td> *Select Option Display Text*</td></tr>
<tr><td>ILike </td><td> Contains</td></tr>
<tr><td>Not ILike </td><td> Does Not Contain</td></tr>
<tr><td>Like </td><td> Contains (Case Sensitive)</td></tr>
<tr><td>Not Like </td><td> Does Not Contain (Case Sensitive)</td></tr>
<tr><td>"=" </td><td> Equal To</td></tr>
<tr><td>&lt;&gt; </td><td> Not Equal To</td></tr>
<tr><td>Is Null </td><td> Is Null</td></tr>
<tr><td>Is Not Null </td><td> Is Not Null</td></tr>
</table>

#### Numeric ####
_(Integer, Long, Short, Float, Double, BigDecimal, BigInteger)_
<table>
*Operator* </td><td> *Select Option Display Text*
<tr><td>"=" </td><td> Equal To</td></tr>
<tr><td>&lt;&gt; </td><td> Not Equal To</td></tr>
<tr><td>&lt; </td><td> Less Than</td></tr>
<tr><td>&lt;= </td><td> Less Than or Equal To</td></tr>
<tr><td>> </td><td> Greater Than</td></tr>
<tr><td>>= </td><td> Greater Than or Equal To</td></tr>
<tr><td>Between </td><td> Between</td></tr>
<tr><td>Is Null </td><td> Is Null</td></tr>
<tr><td>Is Not Null </td><td> Is Not Null</td></tr>
</table>

####  Date ####
<table>
<tr><td>*Operator* </td><td> *Select Option Display Text*</td></tr>
<tr><td>"=" </td><td> Equal To</td></tr>
<tr><td>&lt;&gt; </td><td> Not Equal To</td></tr>
<tr><td>&lt; </td><td> Less Than</td></tr>
<tr><td>&lt;= </td><td> Less Than or Equal To</td></tr>
<tr><td>&gt; </td><td> Greater Than</td></tr>
<tr><td>&gt;= </td><td> Greater Than or Equal To</td></tr>
<tr><td>Between </td><td> Between</td></tr>
<tr><td>Is Null </td><td> Is Null</td></tr>
<tr><td>Is Not Null </td><td> Is Not Null</td></tr>
</table>

#### Boolean ####
<table>
<tr><td>*Operator* </td><td> *Select Option Display Text*</td></tr>
<tr><td>"=" </td><td> Equal To</td></tr>
<tr><td>&lt;&gt; </td><td> Not Equal To</td></tr>
<tr><td>Is Null </td><td> Is Null</td></tr>
<tr><td>Is Not Null </td><td> Is Not Null</td></tr>
</table>

#### Enum ####
_(since 0.6)_
<table>
<tr><td>*Operator* </td><td> *Select Option Display Text*</td></tr>
<tr><td>"=" </td><td> Equal To</td></tr>
<tr><td>&lt;&gt; </td><td> Not Equal To</td></tr>
</table>

#### Domain Constraint Modifications ####
If a property is not nullable (constraint nullable="false"), the *Is Null* and *Is Not Null* operators will not be available for that property.

### Tags ###
#### includes ####
The includes tag should be used in the head section of your pages.  It includes the necessary stylesheet and javascript file for the plugin.
<table>
<tr><td>*Attribute Name* </td><td> *Required* </td><td> *Default Value* </td><td> *Description*</td></tr>
<tr><td>css </td><td> No </td><td> true </td><td> Set to false to exclude the <link> tag for the stylesheet.</td></tr>
<tr><td>js </td><td> No </td><td> true </td><td> Set to false to exclude the <script> tag that supports the filterPane plugin.</td></tr>
</table>

#### currentCriteria ####
This tag renders an unordered list of the currently applied filter criteria, along with links to remove individual filter criteria.
<table>
<tr><td>*Attribute Name* </td><td> *Required* </td><td> *Default Value* </td><td> *Description*</td></tr>
<tr><td>domain </td><td> Yes </td><td> None </td><td> The domain bean being filtered.  May be a String (e.g. "Book") or a class instance (e.g. ${Book})</td></tr>
<tr><td>id </td><td> No </td><td> filterPaneCurrentCriteria </td><td> The id of the unordered list.</td></tr>
<tr><td>title </td><td> No </td><td> None </td><td> The title attribute for the unordered list.</td></tr>
<tr><td>class </td><td> No </td><td> None </td><td> The CSS class to apply to the list.</td></tr>
<tr><td>style </td><td> No </td><td> None </td><td> The style attribute for the list.</td></tr>
<tr><td>dateFormat </td><td> No </td><td> yyyy-MM-dd HH:mm:ss </td><td> The format to apply when displaying date criteria.</td></tr>
<tr><td>action </td><td> No </td><td> filter </td><td> The controller action to submit to when removing criteria.  Set this to the same as your filterPane tag's action attribute.</td></tr>
<tr><td>removeImgDir </td><td> No </td><td> None </td><td> The directory the remove image file is located in.</td></tr>
<tr><td>removeImgFile </td><td> No </td><td> None </td><td> The image filename to be used for the remove link.  Note that if either removeImgDir or removeImgFile are missing, the text "(X)" will be used for the remove link.</td></tr>
<tr><td>quoteValues </td><td> No </td><td> True </td><td> (Since 2.0.1) If true, values will be quoted when displayed.</td></tr>
<tr><td>filterPropertyValues </td><td> No </td><td> None </td><td> (Since 2.0.1) Identical syntax to the filterpane tag's property of the same name.  Use this to set a display property of enum properties.  Ex: ```filterPropertyValues="${[bookType:[displayProperty:'display']]}"```</td></tr>
</table>

#### filterButton ####
This tag renders an HTML link that shows the filter pane when clicked.  When one or more filters are applied, this button will have the @filter-applied@ css class.
<table>
*Attribute Name* </td><td> *Required* </td><td> *Default Value* </td><td> *Description*
<tr><td>text</td><td> No </td><td> "Filter" </td><td> The text that is displayed on the button.</td></tr>
<tr><td>appliedText </td><td> No </td><td> "Change Filter" </td><td> The text displayed on the button when one or more filters are applied.</td></tr>
<tr><td>id </td><td> No </td><td> "filterpane" </td><td> The id of the html element.</td></tr>
<tr><td>textKey </td><td> No </td><td> "fp.tag.filterButton.text" </td><td> The message bundle key that contains the text of this button. (i18n)</td></tr>
<tr><td>appliedTextKey </td><td> No </td><td> "fp.tag.filterButton.appliedText" </td><td> The message bundle key that contains the applied text of this button. (i18n)</td></tr>
</table>

#### filterPane ####
This tag generates the filter pane itself.  As of release 0.4, this tag pulls as much filtering information from the domain class as possible by default.  All attributes from 0.3.1 are still supported, but are considered deprecated in favor of more sensible alternatives.
<table>
<tr><td>*Attribute Name* </td><td> *Required* </td><td> *Default Value* </td><td> *Description*</td></tr>
<tr><td>domain </td><td> Yes </td><td> None </td><td> The name of the Grails domain class to be filtered.  This can either be a string (e.g. "Book"), or an actual class instance (e.g. "${Book}")</td></tr>
<tr><td>dialog </td><td> Yes </td><td> False </td><td> Set to true to cause the rendered filterpane to be displayed as a popup dialog instead of rendered inline in the page.</td></tr>
<tr><td>title </td><td> No </td><td> "Filter" </td><td> The title text that is displayed at the top of the filter pane.</td></tr>
<tr><td>titleKey </td><td> No </td><td> None </td><td> A message bundle key for looking up title text. (i18n)</td></tr>
<tr><td>id </td><td> No </td><td> "filterpane" </td><td> The id of the container div that holds the filter pane.</td></tr>
<tr><td>class </td><td> No </td><td> None </td><td> The class attribute to add to the container div that holds the filter pane.</td></tr>
<tr><td>style </td><td> No </td><td> None </td><td> The style attribute to add to the container div that holds the filter pane.</td></tr>
<tr><td>formName </td><td> No </td><td> "filterForm" </td><td> The name of the filter form element.  Useful if you will use custom javascript on the form.</td></tr>
<tr><td>filterProperties </td><td> No </td><td> None </td><td> If specified then no default properties are included, only those specified.</td></tr>
<tr><td>associatedProperties </td><td> No </td><td> None </td><td> Use this if you wish to filter any properties of associated domain objects (e.g. author.lastName).  The value may either be a comma-delimited string, or a List of strings.</td></tr>
<tr><td>additionalProperties </td><td> No </td><td> None </td><td> By default, identifier, version, and lastUpdated properties are not available to filter by.  Use this attribute to allow them to be filtered.  The value may either be a comma-delimited string, or a List of strings.  Valid values are "id", "identifier", "version", and "lastUpdated"</td></tr>
<tr><td>excludeProperties </td><td> No </td><td> None </td><td> By default all persistent properties of the domain object are filterable.  If you wish to exclude any properties from filtering, specify them in this attribute.  The value may either be a comma-delimited string, or a List of strings.</td></tr>
<tr><td>action </td><td> No </td><td> "filter" </td><td> The controller action to call when the filter is applied.</td></tr>
<tr><td>filterPropertyValues </td><td> No </td><td> None </td><td> A map of property values to pass through to each property's value form control.  For example, since Date properties render a date picker control, you could pass the following to limit the years in the date picker: ```filterPropertyValues="${[someDateProperty:[years:2015..1999]]}"``` or get the values from the database: ```filterPropertyValues="${['author.lastName':[values:Author.executeQuery('select t.lastName from Author t')], 'readPriority.name':[values:ReadPriority.list()]]}"```</td></tr>
<tr><td>customForm </td><td> No </td><td> false </td><td> If true or "true", the tag will not render the surrounding form or the "Apply" button on it.  This is left to the developer.  This attribute is useful if you want to embed the filterpane form in an existing form. (since 0.6.4)  See the example below:</td></tr>
</table>
```
<form id="author-form" name="author-form" method="post">
	 <filterpane:filterPane id="author-filter-pane" domainBean="com.zeddware.grails.plugins.filterpane.Author"
            associatedProperties="books.title, books.bookType"
            titleKey="fp.tag.filterPane.titleText" customForm="true"
	    formName="author-form"/>
	<g:actionSubmit value="Apply Filter From Outside Filter Pane" action="filterCustomForm" />
</form>
```
#### isFiltered ####
This is a logical tag that will render its body if any filters are applied.
<table>
<tr><td>*Attribute Name* </td><td> *Requred* </td><td> *Default Value* </td><td> *Description*</td></tr>
<tr><td>(None) </td><td> </td><td> </td><td></td></tr>
</table>

#### isNotFiltered ####
This is a logical tag that will render its body if no filters are applied.
<table>
<tr><td>*Attribute Name* </td><td> *Requred* </td><td> *Default Value* </td><td> *Description*</td></tr>
<tr><td>(None) </td><td> </td><td> </td><td></td></tr>
</table>

#### paginate ####
This is a convenience tag that may be used in place of the grails paginate tag.  It encapsulates adding the appropriate parameters to the grails paginate tag for the developer.
<table>
<tr><td>*Attribute Name* </td><td> *Required* </td><td> *Default Value* </td><td> *Description*</td></tr>
<tr><td>total </td><td> Yes </td><td> None </td><td> A numeric total to be used when calculating pages.  This is the same total that is passed to the Grails paginate tag.</td></tr>
<tr><td>domainBean </td><td> Yes, if total's value is null </td><td> None </td><td> If total's value is null, this tag is used to perform a count for the user and pass that to the Grails paginate tag.  If not specified and total's value is null, 0 will be used as the total count.</td></tr>
</table>

## Internationalization (i18n) ##
The following keys are supported in message bundles for internationalizing the text in the plugin.

###  Filter Operators ###
* fp.op.ILike
* fp.op.NotILike
* fp.op.Like
* fp.op.NotLike
* fp.op.Equal
* fp.op.NotEqual
* fp.op.IsNull
* fp.op.IsNotNull
* fp.op.GreaterThan
* fp.op.GreaterThanEquals
* fp.op.LessThan
* fp.op.LessThanEquals
* fp.op.Between

### Property Name Display ###
By default, each property's natural name is displayed .  To override this, use keys of the form: @fp.property.text.propertyName@ , where @propertyname@ is the domain property name.  For associated properties, use keys of the form: @fp.property.text.associatedBean.propertyName@ .

### Filter Button Tag ###
If no textKey or appliedTextKey attributes are given, the default keys are checked.
* fp.tag.filterButton.text
* fp.tag.filterButton.appliedText

###  Filter Pane Tag ###
The filterPane tag's keys are listed below, along with their default values for clarity.
* fp.tag.filterPane.titleText=Filter
* fp.tag.filterPane.property.boolean.true=Yes
* fp.tag.filterPane.property.boolean.false=No
* fp.tag.filterPane.property.betweenValueSeparatorText=and
* fp.tag.filterPane.sort.orderByText=Order by
* fp.tag.filterPane.sort.noSelection.text=Select a Property
* fp.tag.filterPane.sort.ascending=Ascending
* fp.tag.filterPane.sort.descending=Descending
* fp.tag.filterPane.button.cancel.text=Cancel
* fp.tag.filterPane.button.clear.text=Clear
* fp.tag.filterPane.button.apply.text=Apply

The full default message bundle can be found in the installed plugin's folder in the messages-filterpane.properties file.

The plugin supports the [i18n Templates](http://www.grails.org/plugin/i18n-templates) plugin's domain property format: ```<domainClass>.<property> <-- for each property```