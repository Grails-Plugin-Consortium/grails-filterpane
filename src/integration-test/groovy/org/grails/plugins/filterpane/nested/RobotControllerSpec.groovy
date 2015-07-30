package org.grails.plugins.filterpane.nested

import grails.core.GrailsApplication
import grails.test.mixin.TestFor
import grails.test.mixin.integration.Integration
import org.grails.plugins.filterpane.FilterPaneService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

@Integration
@TestFor(RobotController)
class RobotControllerSpec extends Specification {
    @Autowired
    FilterPaneService filterPaneService

    @Autowired
    GrailsApplication grailsApplication

    def setup() {
        filterPaneService = new FilterPaneService()
        filterPaneService.grailsApplication = grailsApplication
        controller.filterPaneService = filterPaneService
    }

    def "test the recursive nesting of filtering with listDistinct true"() {
        given:
        Map parameters = ['filter': [op: ['parts': ['functions': ['name': 'ILike']]], 'parts': ['functions': ['name': 'motion']]], listDistinct: true, uniqueCountColumn: 'id']
        Robot.withNewSession {
            Robot.findOrSaveWhere(name: 'wally')
                    .addToParts(Part.findOrSaveWhere(name: 'eyes')
                    .addToFunctions(Function.findOrSaveWhere(name: 'vision'))
                    .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                    .addToFunctions(Function.findOrSaveWhere(name: 'sight')))
                    .addToParts(Part.findOrSaveWhere(name: 'treads')
                    .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                    .addToFunctions(Function.findOrSaveWhere(name: 'flight')))
                    .save(flush: true, failOnError: true)
            Robot.findOrSaveWhere(name: 'jonny5')
                    .addToParts(Part.findOrSaveWhere(name: 'eye')
                    .addToFunctions(Function.findOrSaveWhere(name: 'vising'))
                    .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                    .addToFunctions(Function.findOrSaveWhere(name: 'seeing')))
                    .addToParts(Part.findOrSaveWhere(name: 'tread')
                    .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                    .addToFunctions(Function.findOrSaveWhere(name: 'flying')))
                    .save(flush: true, failOnError: true)
            Robot.findOrSaveWhere(name: 'supreme commander')
                    .addToParts(Part.findOrSaveWhere(name: 'laser')
                    .addToFunctions(Function.findOrSaveWhere(name: 'zapping')))
                    .save(flush: true, failOnError: true)
        }

        when:
        def model
        controller.params.putAll(parameters)
        Robot.withNewSession {
            controller.filter()
            model = controller.modelAndView.model
        }

        then:
        model.robotList.size() == 2
        model.robotCount == 2
        model.robotList.find { it.name == 'wally' }
    }

    def "test the recursive nesting of filtering"() {
        given:
        Robot.withNewSession {
            Robot.findOrSaveWhere(name: 'wally')
                    .addToParts(Part.findOrSaveWhere(name: 'eyes')
                    .addToFunctions(Function.findOrSaveWhere(name: 'vision'))
                    .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                    .addToFunctions(Function.findOrSaveWhere(name: 'sight')))
                    .addToParts(Part.findOrSaveWhere(name: 'treads')
                    .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                    .addToFunctions(Function.findOrSaveWhere(name: 'flight')))
                    .save(flush: true, failOnError: true)
            Robot.findOrSaveWhere(name: 'jonny5')
                    .addToParts(Part.findOrSaveWhere(name: 'eye')
                    .addToFunctions(Function.findOrSaveWhere(name: 'vising'))
                    .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                    .addToFunctions(Function.findOrSaveWhere(name: 'seeing')))
                    .addToParts(Part.findOrSaveWhere(name: 'tread')
                    .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                    .addToFunctions(Function.findOrSaveWhere(name: 'flying')))
                    .save(flush: true, failOnError: true)
            Robot.findOrSaveWhere(name: 'supreme commander')
                    .addToParts(Part.findOrSaveWhere(name: 'laser')
                    .addToFunctions(Function.findOrSaveWhere(name: 'zapping')))
                    .save(flush: true, failOnError: true)
        }

        when:
        def model
        controller.params.filter = [op: ['parts': ['functions': ['name': 'ILike']]], 'parts': ['functions': ['name': 'motion']]]

        Robot.withNewSession {
            controller.filter()
            model = controller.modelAndView.model
        }

        then: 'Counts are incorrect due to missing uniqueness'
        model.robotList.size() == 4
        model.robotCount == 4
        model.robotList.find { it.name == 'wally' }
    }
}
