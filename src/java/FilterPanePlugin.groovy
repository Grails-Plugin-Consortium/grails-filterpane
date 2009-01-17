/**
 *
 * @author steve.krenek
 */
class FilterPanePlugin {
	def version = 0.1
    def author  = "Steve Krenek"
    def authorEmail = "steve.krenek@gmail.com"
    def title = "This plugin adds filtering capabilities to a Grails application's list pages."
    def description = """\
This plugin adds automatic filtering capabilities to any Grails application. The main features of this plugin include:
* Easy integration with Grails list pages via custom tags
* Smart operator display. Only practical filter operations are available for a property's data type. (e.g. no "like" operators for numeric properties)
* Support for major operators including =, <>, >, <, >=, <=, like, ilike, between, is null, and is not null.
* Smart filter value entry. Date properties display a date picker, boolean's display radio buttons, etc.
* Support for a custom list of values, such as a filtering a text property with a constrained list of values.
* Works with Grails list sorting out of the box
* Works with Grails pagination out of the box"""
}

