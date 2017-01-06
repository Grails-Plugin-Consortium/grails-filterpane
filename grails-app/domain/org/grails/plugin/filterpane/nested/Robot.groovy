package org.grails.plugin.filterpane.nested

class Robot {

    String name = 'Bob'
	Overlord overlord;

    static hasMany = [parts: Part]

    static constraints = {
        parts nullable:  true
		overlord nullable: true
    }
	
	@Override
	public String toString() {
		return "id: ${id}, name: ${name}"
	}
}
