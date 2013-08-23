package org.grails.plugin.filterpane.nested

import grails.plugin.spock.IntegrationSpec

class RobotControllerSpec extends IntegrationSpec {

    def "test the recursive nesting of filtering with listDistinct true"() {
        given:
        RobotController robotController = new RobotController()
        Robot.findOrSaveWhere(name: 'wally')
                .addToParts(Part.findOrSaveWhere(name: 'eyes')
                                    .addToFunctions(Function.findOrSaveWhere(name: 'vision'))
                                    .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                                    .addToFunctions(Function.findOrSaveWhere(name: 'sight')))
                .addToParts(Part.findOrSaveWhere(name: 'treads')
                                    .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                                    .addToFunctions(Function.findOrSaveWhere(name: 'flight')))
        Robot.findOrSaveWhere(name: 'jonny5')
                .addToParts(Part.findOrSaveWhere(name: 'eye')
                                    .addToFunctions(Function.findOrSaveWhere(name: 'vising'))
                                    .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                                    .addToFunctions(Function.findOrSaveWhere(name: 'seeing')))
                .addToParts(Part.findOrSaveWhere(name: 'tread')
                                    .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                                    .addToFunctions(Function.findOrSaveWhere(name: 'flying')))
        Robot.findOrSaveWhere(name: 'supreme commander')
                .addToParts(Part.findOrSaveWhere(name: 'laser')
                                    .addToFunctions(Function.findOrSaveWhere(name: 'zapping')))

        when:
        robotController.params.filter = [op: ['parts': ['functions': ['name': 'ILike']]], 'parts': ['functions': ['name': 'motion']]]
        robotController.params.listDistinct = true
        robotController.params.uniqueCountColumn = 'id'

        robotController.filter()
        def model = robotController.modelAndView.model

        then:
        model.robotList.size() == 2
        model.robotCount == 2
        model.robotList.find { it.name == 'wally'}
    }

    def "test the recursive nesting of filtering"() {
        given:
        RobotController robotController = new RobotController()
        Robot.findOrSaveWhere(name: 'wally')
                .addToParts(Part.findOrSaveWhere(name: 'eyes')
                                    .addToFunctions(Function.findOrSaveWhere(name: 'vision'))
                                    .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                                    .addToFunctions(Function.findOrSaveWhere(name: 'sight')))
                .addToParts(Part.findOrSaveWhere(name: 'treads')
                                    .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                                    .addToFunctions(Function.findOrSaveWhere(name: 'flight')))
        Robot.findOrSaveWhere(name: 'jonny5')
                .addToParts(Part.findOrSaveWhere(name: 'eye')
                                    .addToFunctions(Function.findOrSaveWhere(name: 'vising'))
                                    .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                                    .addToFunctions(Function.findOrSaveWhere(name: 'seeing')))
                .addToParts(Part.findOrSaveWhere(name: 'tread')
                                    .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                                    .addToFunctions(Function.findOrSaveWhere(name: 'flying')))
        Robot.findOrSaveWhere(name: 'supreme commander')
                .addToParts(Part.findOrSaveWhere(name: 'laser')
                                    .addToFunctions(Function.findOrSaveWhere(name: 'zapping')))

        when:
        robotController.params.filter = [op: ['parts': ['functions': ['name': 'ILike']]], 'parts': ['functions': ['name': 'motion']]]
        //robotController.params.listDistinct = true
        //robotController.params.distinct = ['column': 'id']

        robotController.filter()
        def model = robotController.modelAndView.model

        then: 'Counts are incorrect due to missing uniqueness'
        model.robotList.size() == 4
        model.robotCount == 4
        model.robotList.find { it.name == 'wally'}
    }
}
