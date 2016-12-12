package org.grails.plugins.filterpane

import com.demo.nested.Function
import com.demo.nested.Part
import com.demo.nested.Robot
import grails.core.GrailsApplication
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification
import spock.lang.Unroll

@Integration
@Rollback
class FilterPaneServiceNestedSpec extends Specification {

    @Autowired
    GrailsApplication grailsApplication

    @Autowired
    FilterPaneService filterPaneService

    def "test the recursive nesting of filtering count"() {
        given:
        def params = ['filter': [op: ['parts': ['functions': ['name': 'ILike']]], 'parts': ['functions': ['name': 'motion']]]]
        Robot robot = Robot.findOrCreateWhere(name: 'wally').save(flush: true)
        Part part1 = Part.findOrCreateWhere(name: 'eyes', robot: robot)
                .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                .addToFunctions(Function.findOrSaveWhere(name: 'vision'))
                .addToFunctions(Function.findOrSaveWhere(name: 'sight')).save(flush: true)
        Part part2 = Part.findOrCreateWhere(name: 'treads', robot: robot)
                .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                .addToFunctions(Function.findOrSaveWhere(name: 'flight')).save(flush: true)
        robot.addToParts(part1)
                .addToParts(part2)
                .save(flush: true, failOnError: true)

        when:
        def robotCount = filterPaneService.count(params, Robot)
        Function.list().each { println it.toString() }

        then: 'count will be technically wrong since unique column not specified'
        Function.list().size() == 4
        Part.list().size() == 2
        Robot.list().size() == 1
        robotCount == 2
    }

    def "test the recursive nesting of filtering count with unique"() {
        given:
        def params = ['uniqueCountColumn': 'id', 'filter': [op: ['parts': ['functions': ['name': 'ILike']]], 'parts': ['functions': ['name': 'motion']]]]
        Robot robot = Robot.findOrCreateWhere(name: 'wally').save(flush: true)
        Part part1 = Part.findOrCreateWhere(name: 'eyes', robot: robot)
                .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                .addToFunctions(Function.findOrSaveWhere(name: 'vision'))
                .addToFunctions(Function.findOrSaveWhere(name: 'sight')).save(flush: true)
        Part part2 = Part.findOrCreateWhere(name: 'treads', robot: robot)
                .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                .addToFunctions(Function.findOrSaveWhere(name: 'flight')).save(flush: true)
        robot.addToParts(part1)
                .addToParts(part2)
                .save(flush: true, failOnError: true)

        when:
        def robotCount = filterPaneService.count(params, Robot)

        then: 'distinct by parent unique id should correct count'
        Function.list().size() == 4
        Part.list().size() == 2
        Robot.list().size() == 1
        robotCount == 1
    }

    def "test the recursive nesting of filtering"() {
        given:
        def params = ['filter': [op: ['parts': ['functions': ['name': 'ILike']]], 'parts': ['functions': ['name': 'motion']]]]

        Function motion = Function.findOrSaveWhere(name: 'motion')
        Function vision = Function.findOrSaveWhere(name: 'vision')
        Function sight = Function.findOrSaveWhere(name: 'sight')
        Function flight = Function.findOrSaveWhere(name: 'flight')
        Robot robot = Robot.findOrCreateWhere(name: 'wally').save(flush: true)
        Part part1 = Part.findOrCreateWhere(name: 'eyes', robot: robot)
                .addToFunctions(motion)
                .addToFunctions(vision)
                .addToFunctions(sight).save(flush: true)
        Part part2 = Part.findOrCreateWhere(name: 'treads', robot: robot)
                .addToFunctions(motion)
                .addToFunctions(flight).save(flush: true)
        robot.addToParts(part1)
                .addToParts(part2)
                .save(flush: true, failOnError: true)

        when:
        def robots = filterPaneService.filter(params, Robot)

        then:
        Function.list().size() == 4
        Part.list().size() == 2
        Robot.list().size() == 1
        robots.size() == 1
    }

    def "test the recursive nesting of filtering with listDistinct true"() {
        given:
        def params = [listDistinct: true, 'uniqueCountColumn': 'id', 'filter': [op: ['parts': ['functions': ['name': 'ILike']]], 'parts': ['functions': ['name': 'motion']]]]
        Robot robot = Robot.findOrCreateWhere(name: 'wally').save(flush: true)
        Part part1 = Part.findOrCreateWhere(name: 'eyes', robot: robot)
                .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                .addToFunctions(Function.findOrSaveWhere(name: 'vision'))
                .addToFunctions(Function.findOrSaveWhere(name: 'sight')).save(flush: true)
        Part part2 = Part.findOrCreateWhere(name: 'treads', robot: robot)
                .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                .addToFunctions(Function.findOrSaveWhere(name: 'flight')).save(flush: true)
        robot.addToParts(part1)
                .addToParts(part2)
                .save(flush: true, failOnError: true)

        when:
        def robots = filterPaneService.filter(params, Robot)

        then: 'distinct by parent unique id should correct count'
        Function.list().size() == 4
        Part.list().size() == 2
        Robot.list().size() == 1
        robots.size() == 1
    }

    def "test the recursive nesting of filtering count and multiple top level objects"() {
        given:
        def params = ['filter': [op: ['parts': ['functions': ['name': 'ILike']]], 'parts': ['functions': ['name': 'motion']]]]
        setupManyRobots()

        when:
        def robotCount = filterPaneService.count(params, Robot)

        then:
        Function.list().size() == 8
        Part.list().size() == 5
        Robot.list().size() == 3
        robotCount == 4
    }

    private void setupManyRobots() {
        Robot robot = Robot.findOrCreateWhere(name: 'wally').save(flush: true)
        Part part1 = Part.findOrCreateWhere(name: 'eyes', robot: robot)
                .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                .addToFunctions(Function.findOrSaveWhere(name: 'vision'))
                .addToFunctions(Function.findOrSaveWhere(name: 'sight')).save(flush: true)
        Part part2 = Part.findOrCreateWhere(name: 'treads', robot: robot)
                .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                .addToFunctions(Function.findOrSaveWhere(name: 'flight')).save(flush: true)
        robot.addToParts(part1)
                .addToParts(part2)
                .save(flush: true, failOnError: true)

        Robot robot2 = Robot.findOrCreateWhere(name: 'jonny5').save(flush: true)
        Part part2_1 = Part.findOrCreateWhere(name: 'eye', robot: robot2)
                .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                .addToFunctions(Function.findOrSaveWhere(name: 'vising'))
                .addToFunctions(Function.findOrSaveWhere(name: 'seeing')).save(flush: true)
        Part part2_2 = Part.findOrCreateWhere(name: 'tread', robot: robot2)
                .addToFunctions(Function.findOrSaveWhere(name: 'motion'))
                .addToFunctions(Function.findOrSaveWhere(name: 'flying')).save(flush: true)
        robot2.addToParts(part2_1)
                .addToParts(part2_2)
                .save(flush: true, failOnError: true)

        Robot robot3 = Robot.findOrCreateWhere(name: 'supreme commander').save(flush: true)
        Part part3_1 = Part.findOrCreateWhere(name: 'laser', robot: robot3)
                .addToFunctions(Function.findOrSaveWhere(name: 'zapping')).save(flush: true)
        robot3.addToParts(part3_1)
                .save(flush: true, failOnError: true)
    }

    def "test the recursive nesting of filtering count with unique and multiple top level objects"() {
        given:
        def params = ['uniqueCountColumn': 'id', 'filter': [op: ['parts': ['functions': ['name': 'ILike']]], 'parts': ['functions': ['name': 'motion']]]]
        setupManyRobots()

        when:
        def robotCount = filterPaneService.count(params, Robot)

        then: 'distinct by parent unique id should correct count'
        Function.list().size() == 8
        Part.list().size() == 5
        Robot.list().size() == 3
        robotCount == 2
    }

    def "test the recursive nesting of filtering and multiple top level objects"() {
        given:
        def params = ['filter': [op: ['parts': ['functions': ['name': 'ILike']]], 'parts': ['functions': ['name': 'motion']]]]
        setupManyRobots()

        when:
        def robots = filterPaneService.filter(params, Robot)

        then: 'count will be technically wrong since listDistinct not specified'
        Function.list().size() == 8
        Part.list().size() == 5
        Robot.list().size() == 3
        robots.size() == 4
    }

    def "test the recursive nesting of filtering with listDistinct true and multiple top level objects"() {
        given:
        def params = [listDistinct: true, 'uniqueCountColumn': 'id', 'filter': [op: ['parts': ['functions': ['name': 'ILike']]], 'parts': ['functions': ['name': 'motion']]]]
        setupManyRobots()

        when:
        def robots = filterPaneService.filter(params, Robot)

        then: 'distinct by parent unique id should correct count'
        Function.list().size() == 8
        Part.list().size() == 5
        Robot.list().size() == 3
        robots.size() == 2
    }

    def "test the recursive nesting of filtering and sorting"() {
        given:
        def params = ['sort': 'parts.functions.name', 'filter': [op: ['parts': ['functions': ['name': 'ILike']]], 'parts': ['functions': ['name': 'motion']]]]
        setupManyRobots()

        when:
        def robots = filterPaneService.filter(params, Robot)

        then: 'count will be technically wrong since listDistinct not specified'
        Function.list().size() == 8
        Part.list().size() == 5
        Robot.list().size() == 3
        robots.size() == 4
    }

    def "test the recursive nesting of filtering with listDistinct true  and sorting"() {
        given:
        def params = ['sort': 'parts.functions.name', listDistinct: true, 'uniqueCountColumn': 'id', 'filter': [op: ['parts': ['functions': ['name': 'ILike']]], 'parts': ['functions': ['name': 'motion']]]]
        setupManyRobots()


        when:
        def robots = filterPaneService.filter(params, Robot)
        def robotCount = filterPaneService.count(params, Robot)

        then: 'distinct by parent unique id should correct count'
        Function.list().size() == 8
        Part.list().size() == 5
        Robot.list().size() == 3
        robots.size() == 2
        robotCount == 2
    }

    def "test the recursive nesting of filtering via actual createCriteria"() {
        given:
        def params = ['sort': 'parts.functions.name', listDistinct: true, 'uniqueCountColumn': 'id', 'filter': [op: ['parts': ['functions': ['name': 'ILike']]], 'parts': ['functions': ['name': 'motion']]]]
        setupManyRobots()

        when:
        def robots = criteria.listDistinct {
            parts {
                functions {
                    eq('name', 'motion')
                }
            }
            order('name', 'desc')
        }

        then: 'distinct by parent unique id should correct count'
        Function.list().size() == 8
        Part.list().size() == 5
        Robot.list().size() == 3
        robots.size() == 2
        robots.get(0).name == 'wally'
        robots.get(1).name == 'jonny5'
    }

    @Unroll
    def "test all the operators filtering like #operator #name distinct=#distinct listDistinct=#listDistinct #expectedCount"() {
        given:
        def params = [listDistinct: listDistinct, 'filter': [op: ['parts': ['functions': ['name': operator]]], 'parts': ['functions': ['name': name]]]]
        if (distinct && distinctColumn) {
            params.distinct = ['column': distinctColumn]
        }

        setupManyRobots()

        when:
        def robotCount = 0
        def robots = filterPaneService.filter(params, Robot)
        if (distinct && distinctColumn) {
            robotCount = filterPaneService.count(params, Robot)
        }

        then:
        robots.size() == listCount
        if (distinct && distinctColumn) {
            robotCount == expectedCount
        }

        where:
        operator | name     | listCount | listDistinct | distinct | distinctColumn | expectedCount
        'ILike'  | 'motion' | 4         | false        | false    | null           | 0
        'ILike'  | 'motion' | 4         | false        | false    | 'id'           | 2
        'ILike'  | 'motion' | 2         | true         | false    | null           | 0
        'ILike'  | 'motion' | 2         | true         | true     | 'id'           | 2
        'ILike'  | 'MOTION' | 4         | false        | false    | null           | 0
        'ILike'  | 'MOTION' | 4         | false        | false    | 'id'           | 2
        'ILike'  | 'MOTION' | 2         | true         | false    | null           | 0
        'ILike'  | 'MOTION' | 2         | true         | true     | 'id'           | 2
        'Like'   | 'motion' | 4         | false        | false    | null           | 0
        'Like'   | 'motion' | 4         | false        | false    | 'id'           | 2
        'Like'   | 'motion' | 2         | true         | false    | null           | 0
        'Like'   | 'motion' | 2         | true         | true     | 'id'           | 2
        'Like'   | 'MOTION' | 0         | false        | false    | null           | 0
        'Like'   | 'MOTION' | 0         | false        | false    | 'id'           | 0
        'Like'   | 'MOTION' | 0         | true         | false    | null           | 0
        'Like'   | 'MOTION' | 0         | true         | true     | 'id'           | 0
    }

    @Unroll
    def "test all the operators filtering equal #operator #name"() {
        given:
        def params = [listDistinct: listDistinct, 'filter': [op: ['parts': ['functions': ['name': operator]]], 'parts': ['functions': ['name': name]]]]
        if (distinct && distinctColumn) {
            params.distinct = ['column': distinctColumn]
        }

        setupManyRobots()

        when:
        def robotCount = 0
        def robots = filterPaneService.filter(params, Robot)
        if (distinct && distinctColumn) {
            robotCount = filterPaneService.count(params, Robot)
        }

        then:
        robots.size() == listCount
        if (distinct && distinctColumn) {
            robotCount == expectedCount
        }

        where:
        operator   | name     | listCount | listDistinct | distinct | distinctColumn | expectedCount
        'Equal'    | 'motion' | 4         | false        | false    | null           | 0
        'Equal'    | 'motion' | 4         | false        | false    | 'id'           | 2
        'Equal'    | 'motion' | 2         | true         | false    | null           | 0
        'Equal'    | 'motion' | 2         | true         | true     | 'id'           | 2
        'Equal'    | 'MOTION' | 0         | false        | false    | null           | 0
        'Equal'    | 'MOTION' | 0         | false        | false    | 'id'           | 0
        'Equal'    | 'MOTION' | 0         | true         | false    | null           | 0
        'Equal'    | 'MOTION' | 0         | true         | true     | 'id'           | 0
        'NotEqual' | 'motion' | 7         | false        | false    | null           | 0
        'NotEqual' | 'motion' | 7         | false        | false    | 'id'           | 2
        'NotEqual' | 'motion' | 3         | true         | false    | null           | 0
        'NotEqual' | 'motion' | 3         | true         | true     | 'id'           | 2
        'NotEqual' | 'MOTION' | 11        | false        | false    | null           | 0
        'NotEqual' | 'MOTION' | 11        | false        | false    | 'id'           | 11
        'NotEqual' | 'MOTION' | 3         | true         | false    | null           | 0
        'NotEqual' | 'MOTION' | 3         | true         | true     | 'id'           | 3
    }
}
