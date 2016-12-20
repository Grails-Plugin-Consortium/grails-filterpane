package org.grails.plugin.filterpane.nested

import spock.lang.Specification

class RobotControllerSpec extends Specification {

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
        model.robotList.find { it.name == 'wally' }
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
        model.robotList.find { it.name == 'wally' }
    }
	
	def "test recursive with sorting on one-to-many"() {
		given:
		RobotController robotController = new RobotController()
		
		Overlord lord1 = Overlord.findOrSaveWhere(name: 'lord a')
		Overlord lord2 = Overlord.findOrSaveWhere(name: 'lord b')
		Overlord lord3 = Overlord.findOrSaveWhere(name: 'lord c')
		
		Robot.findOrSaveWhere(name: 'EVE 1', overlord: lord3)
				.addToParts(Part.findOrSaveWhere(name: 'da')
				.addToFunctions(Function.findOrSaveWhere(name: 'f1'))
				.addToFunctions(Function.findOrSaveWhere(name: 'f2')))
		Robot.findOrSaveWhere(name: 'EVE 2', overlord: lord1)
				.addToParts(Part.findOrSaveWhere(name: 'ba')
				.addToFunctions(Function.findOrSaveWhere(name: 'f1'))
				.addToFunctions(Function.findOrSaveWhere(name: 'f2')))
		Robot.findOrSaveWhere(name: 'EVE 3', overlord: lord2)
				.addToParts(Part.findOrSaveWhere(name: 'aa')
				.addToFunctions(Function.findOrSaveWhere(name: 'f1'))
				.addToFunctions(Function.findOrSaveWhere(name: 'f2')))
		Robot.findOrSaveWhere(name: 'EVE 4', overlord: lord2)
				.addToParts(Part.findOrSaveWhere(name: 'ca')
				.addToFunctions(Function.findOrSaveWhere(name: 'f1'))
				.addToFunctions(Function.findOrSaveWhere(name: 'f2')))
		Robot.findOrSaveWhere(name: 'HK-47', overlord: lord1)
				.addToParts(Part.findOrSaveWhere(name: 'aa')
				.addToFunctions(Function.findOrSaveWhere(name: 'f1'))
				.addToFunctions(Function.findOrSaveWhere(name: 'f2')))
		

		when:
		robotController.params.filter = ['name': "EVE", op: ['name': 'ILike', 'parts': ['functions': ['name': 'ILike']]]]
		robotController.params.sort = 'overlord.name'
		robotController.params.order = 'desc'
		robotController.params.listDistinct = true
		robotController.params.uniqueCountColumn = 'id'

		robotController.filter()
		def model = robotController.modelAndView.model

		then:
		println model.robotList
		model.robotList.size() == 4
		model.robotCount == 4
		model.robotList[0].name.startsWith('EVE')
		
		/*
		 * the sort & order (desc) should output the 4 EVE Robots by their overlord's name, so
		 * EVE 1
		 * EVE 3 & 4 (same overlord)
		 * EVE 2
		 */
		
		model.robotList[0].name == 'EVE 1'
		model.robotList[3].name == 'EVE 2'
		model.robotList[0].overlord.name == 'lord c'
		model.robotList[1].overlord.name == 'lord b'
		model.robotList[2].overlord.name == 'lord b'
		model.robotList[3].overlord.name == 'lord a'
	}
}
