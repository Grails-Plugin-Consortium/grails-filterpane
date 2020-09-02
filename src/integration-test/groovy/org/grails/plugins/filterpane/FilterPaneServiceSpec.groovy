package org.grails.plugins.filterpane

import com.demo.Author
import com.demo.Book
import com.demo.BookType
import grails.core.GrailsApplication
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.orm.hibernate5.HibernateQueryException
import spock.lang.Specification
import spock.lang.Unroll

@Integration
@Rollback
class FilterPaneServiceSpec extends Specification {

    @Autowired
    FilterPaneService filterPaneService
    @Autowired
    GrailsApplication grailsApplication

    def "test nested criteria call dot notation"() {
        setup:
        def books
//        Book.withSession {
        createBookWithAuthors()
//        }

        when:
        Book.withSession {
            books = Book.createCriteria().list {
                and {
                    eq('coAuthor.firstName', 'Cool')
                }
            }
        }

        then:
        thrown(HibernateQueryException)
    }

    def "test nested criteria call"() {
        setup:
        def books
        Book.withSession {
            createBookWithAuthors()
        }

        when:
        Book.withSession {
            books = Book.createCriteria().listDistinct {
                and {
                    'authors' {
                        eq('lastName', 'Dude')
                        order('lastName', 'asc')
                    }
                }
            }
        }

        then:
        Book.list().size() == 2
        books.size() == 1
    }

    def "test nested criteria call without distinct"() {
        setup:
        def books
        Book.withSession {
            createBookWithAuthors()
        }

        when:
        Book.withSession {
            books = Book.createCriteria().list {
                and {
                    'authors' {
                        eq('lastName', 'Dude')
                        order('lastName', 'asc')
                    }
                }
            }
        }

        then: 'Incorrect size due to multiple matched children'
        Book.list().size() == 2
        books.size() == 2
    }

    def "test emdash filtering"() {
        given:
        def params = ['filter': [op: [title: 'ILike'], title: 'how']]
        new Book(title: 'Hello�how are you').save(flush: true)

        when:
        def results = filterPaneService.filter(params, Book)

        then:
        Book.list().size() == 1
        1 == results?.size()
    }

    def "filter books no count"() {
        setup:
        def params = ['filter': [op: [title: 'ILike'], title: 'think']]
        setupFilterBooksNoCount()

        when:
        def results = filterPaneService.filter(params, Book)

        then:
        Book.list().size() == 3
        2 == results?.size()
    }

    private void setupFilterBooksNoCount() {
        Book.findOrSaveWhere(title: 'i like to think')
        Book.findOrSaveWhere(title: 'think about it')
        Book.findOrSaveWhere(title: 'i love turtles')
    }

    def "get books by id"() {
        given:
        def book = Book.findOrSaveWhere(title: 'i like to think')
        def book2 = Book.findOrSaveWhere(title: 'i like to think more')
        def params = ['filter': [op: [id: 'Equal'], id: book.id]]

        when:
        List<Book> books = (List<Book>) filterPaneService.filter(params, Book)

        then:
        books.size() == 1
        books[0].id == book.id
        books[0] == book
    }

    def "get author count for book by title"() {
        given:
        def params = ['filter': [op: ['authors': ['lastName': 'ILike']], 'authors': ['lastName': 'Dude']]]
        Book.findOrSaveWhere(title: 'i hate bears')
        Book.findOrSaveWhere(title: 'i hate zombies')
        Book.findOrSaveWhere(title: 'i like turtles')
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Cool', lastName: 'Dude'))
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Another', lastName: 'Dude')).save(flush: true)

        when:
        def books = filterPaneService.count(params, Book)

        then: 'count will be = number of authors in book since unique column not specified'
        Book.list().size() == 3
        books == 2
    }

    def "get author count for book by title and unique column"() {
        given:
        def params = ['uniqueCountColumn': 'id', 'filter': [op: ['authors': ['lastName': 'ILike']], 'authors': ['lastName': 'Dude']]]
        Book.findOrSaveWhere(title: 'i hate bears')
        Book.findOrSaveWhere(title: 'i hate zombies')
        Book.findOrSaveWhere(title: 'i like turtles')
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Cool', lastName: 'Dude'))
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Another', lastName: 'Dude')).save(flush: true)

        when:
        def books = filterPaneService.count(params, Book)

        then: 'count will be fixed due to using distinct column on parent'
        Book.list().size() == 3
        books == 1
    }

    def "get author for book by association filter"() {
        given:
        def params = ['filter': [op: ['authors': ['lastName': 'Equal']], 'authors': ['lastName': 'Dude']]]
        Book.findOrSaveWhere(title: 'i like turtlesbearsrabbits')
        def book2 = Book.findOrSaveWhere(title: 'i like turtles')
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Cool', lastName: 'Dude'))
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Another', lastName: 'Dude')).save(flush: true)

        when:
        List<Book> books = (List<Book>) filterPaneService.filter(params, Book)

        then: 'since non-distinct count will be = number of authors'
        Book.list().size() == 2
        books.size() == 2
        books[0].authors.size() == 2
        books[0].authors.find { it.firstName == 'Cool' }
        books[0].authors.find { it.firstName == 'Another' }
    }

    def "get author for book by association filter using listDistinct"() {
        given:
        def params = [listDistinct: true, filter: [op: ['authors': ['lastName': 'Equal']], 'authors': ['lastName': 'Dude']]]
        Book.findOrSaveWhere(title: 'i like turtlesbearsrabbits')
        def book2 = Book.findOrSaveWhere(title: 'i like turtles')
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Cool', lastName: 'Dude'))
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Another', lastName: 'Dude')).save(flush: true)

        when:
        List<Book> books = (List<Book>) filterPaneService.filter(params, Book)

        then:
        Book.list().size() == 2
        books.size() == 1
        books[0].authors.size() == 2
        books[0].authors.find { it.firstName == 'Cool' }
        books[0].authors.find { it.firstName == 'Another' }
    }

    def "test for empty params"() {
        given:
        def params = [:]
        5.times {
            Book.findOrSaveWhere(title: "Book ${it}")
        }

        when:
        List<Book> books = (List<Book>) filterPaneService.filter(params, Book)

        then:
        books.size() == 5
        5.times {
            books.find { it.title == "Book ${it}" }
        }
    }

    def "get author count for book by title using begins with"() {
        given:
        def params = ['filter': [op: ['authors': ['lastName': 'BeginsWith']], 'authors': ['lastName': 'Dude']]]
        Book.findOrSaveWhere(title: 'i hate bears')
        Book.findOrSaveWhere(title: 'i hate zombies')
        Book.findOrSaveWhere(title: 'i like turtles')
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Cool', lastName: 'Dude One'))
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Another', lastName: 'Another Dude')).save(flush: true)

        when:
        def books = filterPaneService.count(params, Book)

        then: 'count will be = number of authors in book since unique column not specified'
        Book.list().size() == 3
        books == 1
    }

    def "get author count for book by title using begins with invalid case"() {
        given:
        def params = ['filter': [op: ['authors': ['lastName': 'BeginsWith']], 'authors': ['lastName': 'dude']]]
        Book.findOrSaveWhere(title: 'i hate bears')
        Book.findOrSaveWhere(title: 'i hate zombies')
        Book.findOrSaveWhere(title: 'i like turtles')
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Cool', lastName: 'Dude One'))
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Another', lastName: 'Another Dude'))

        when:
        def books = filterPaneService.count(params, Book)

        then: 'count will be = number of authors in book since unique column not specified'
        Book.list().size() == 3
        books == 0
    }

    def "get author count for book by title using begins with case insensitive"() {
        given:
        def params = ['filter': [op: ['authors': ['lastName': 'IBeginsWith']], 'authors': ['lastName': 'dude']]]
        Book.findOrSaveWhere(title: 'i hate bears')
        Book.findOrSaveWhere(title: 'i hate zombies')
        Book.findOrSaveWhere(title: 'i like turtles')
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Cool', lastName: 'Dude One'))
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Another', lastName: 'Another Dude')).save(flush: true)

        when:
        def books = filterPaneService.count(params, Book)

        then: 'count will be = number of authors in book since unique column not specified'
        Book.list().size() == 3
        books == 1
    }

    def "get author count for book by title using begins with case insensitive caps"() {
        given:
        def params = ['filter': [op: ['authors': ['lastName': 'IBeginsWith']], 'authors': ['lastName': 'DUDE']]]
        Book.findOrSaveWhere(title: 'i hate bears')
        Book.findOrSaveWhere(title: 'i hate zombies')
        Book.findOrSaveWhere(title: 'i like turtles')
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Cool', lastName: 'Dude One'))
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Another', lastName: 'Another Dude')).save(flush: true)

        when:
        def books = filterPaneService.count(params, Book)

        then: 'count will be = number of authors in book since unique column not specified'
        Book.list().size() == 3
        books == 1
    }

    def "get author count for book by title using ends with"() {
        given:
        def params = ['filter': [op: ['authors': ['lastName': 'EndsWith']], 'authors': ['lastName': 'Dude']]]
        Book.findOrSaveWhere(title: 'i hate bears')
        Book.findOrSaveWhere(title: 'i hate zombies')
        Book.findOrSaveWhere(title: 'i like turtles')
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Cool', lastName: 'Dude One'))
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Another', lastName: 'Another Dude')).save(flush: true)

        when:
        def books = filterPaneService.count(params, Book)

        then: 'count will be = number of authors in book since unique column not specified'
        Book.list().size() == 3
        books == 1
    }

    def "get author count for book by title using ends with invalid case"() {
        given:
        def params = ['filter': [op: ['authors': ['lastName': 'EndsWith']], 'authors': ['lastName': 'dude']]]
        Book.findOrSaveWhere(title: 'i hate bears')
        Book.findOrSaveWhere(title: 'i hate zombies')
        Book.findOrSaveWhere(title: 'i like turtles')
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Cool', lastName: 'Dude One'))
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Another', lastName: 'Another Dude'))

        when:
        def books = filterPaneService.count(params, Book)

        then: 'count will be = number of authors in book since unique column not specified'
        Book.list().size() == 3
        books == 0
    }

    def "get author count for book by title using ends with case insensitive"() {
        given:
        def params = ['filter': [op: ['authors': ['lastName': 'IEndsWith']], 'authors': ['lastName': 'dude']]]
        Book.findOrSaveWhere(title: 'i hate bears')
        Book.findOrSaveWhere(title: 'i hate zombies')
        Book.findOrSaveWhere(title: 'i like turtles')
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Cool', lastName: 'Dude One'))
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Another', lastName: 'Another Dude')).save(flush: true)

        when:
        def books = filterPaneService.count(params, Book)

        then: 'count will be = number of authors in book since unique column not specified'
        Book.list().size() == 3
        books == 1
    }

    def "get author count for book by title using ends with case insensitive caps"() {
        given:
        def params = ['filter': [op: ['authors': ['lastName': 'IEndsWith']], 'authors': ['lastName': 'DUDE']]]
        Book.findOrSaveWhere(title: 'i hate bears')
        Book.findOrSaveWhere(title: 'i hate zombies')
        Book.findOrSaveWhere(title: 'i like turtles')
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Cool', lastName: 'Dude One'))
                .addToAuthors(Author.findOrSaveWhere(firstName: 'Another', lastName: 'Another Dude')).save(flush: true)

        when:
        def books = filterPaneService.count(params, Book)

        then: 'count will be = number of authors in book since unique column not specified'
        Book.list().size() == 3
        books == 1
    }

    @Unroll
    def "test all the operators filtering #operator #title"() {
        given:
        def params = ['filter': [op: ['title': operator], 'title': title]]
        Book.findOrSaveWhere(title: 'i like turtlesbearsrabbits')
        Book.findOrSaveWhere(title: 'i like turtles bears rabbits')
        Book.findOrSaveWhere(title: 'i like zombies')
        Book.findOrSaveWhere(title: null)

        when:
        List<Book> books = (List<Book>) filterPaneService.filter(params, Book)

        then:
        books.size() == listCount

        where:
        operator      | title            | listCount
        'ILike'       | 'BEARS*'         | 2
        'ILike'       | '*ZOMBIES'       | 1
        'ILike'       | '*zombies'       | 1
        'Like'        | 'turtles'        | 2
        'Like'        | '*turtles'       | 2
        'Like'        | '*turtles*'      | 2
        'Like'        | 'TURTLES'        | 0
        'Like'        | '*TURTLES'       | 0
        'Like'        | '*TURTLES*'      | 0
        'Like'        | 'i like zombies' | 1
        'Like'        | '*LIKE*'         | 0
        'Like'        | '*like*'         | 3
        'Equal'       | 'like'           | 0
        'Equal'       | 'i like zombies' | 1
        'Equal'       | 'I LIKE ZOMBIES' | 0
        'NotEqual'    | 'i like zombies' | 2
        'NotEqual'    | 'zombies'        | 3
        'NotLike'     | 'zombies'        | 2
        'NotLike'     | '*zombies'       | 2
        'NotLike'     | '*like*'         | 0
        'NotILike'    | '*turtles*'      | 1
        'NotILike'    | '*TURTLES*'      | 1
        'NotILike'    | '*ZOMBIES'       | 2
        'IsNull'      | 'unused'         | 1
        'BeginsWith'  | 'i like'         | 3
        'BeginsWith'  | 'I LIKE'         | 0
        'BeginsWith'  | 'like'           | 0
        'BeginsWith'  | 'LIKE'           | 0
        'IBeginsWith' | 'i like'         | 3
        'IBeginsWith' | 'I LIKE'         | 3
        'IBeginsWith' | 'like'           | 0
        'IBeginsWith' | 'LIKE'           | 0
        'EndsWith'    | 'rabbits'        | 2
        'EndsWith'    | 'RABBITS'        | 0
        'EndsWith'    | 'like'           | 0
        'EndsWith'    | 'LIKE'           | 0
        'IEndsWith'   | 'rabbits'        | 2
        'IEndsWith'   | 'RABBITS'        | 2
        'IEndsWith'   | 'like'           | 0
        'IEndsWith'   | 'LIKE'           | 0
    }


    def "get book by single bookType InList"() {
        given:
        def params = [filter: [op: ['bookType': 'InList'], 'bookType': [BookType.Fiction.toString()].toArray()]]
        Book.findOrSaveWhere(bookType: BookType.Fiction)
        Book.findOrSaveWhere(bookType: BookType.Reference)
        Book.findOrSaveWhere(bookType: BookType.NonFiction)

        when:
        List<Book> books = (List<Book>) filterPaneService.filter(params, Book)

        then:
        Book.list().size() == 3
        books.size() == 1
        books[0].bookType == BookType.Fiction
    }

    def "get book by single bookType NotInList"() {
        given:
        def params = [filter: [op: ['bookType': 'NotInList'], 'bookType': [BookType.Fiction.toString()].toArray()]]
        Book.findOrSaveWhere(bookType: BookType.Fiction)
        Book.findOrSaveWhere(bookType: BookType.Reference)
        Book.findOrSaveWhere(bookType: BookType.NonFiction)

        when:
        List<Book> books = (List<Book>) filterPaneService.filter(params, Book)

        then:
        Book.list().size() == 3
        books.size() == 2
        books.findAll { it.bookType == BookType.Fiction }.size() == 0
        books.findAll { it.bookType == BookType.Reference }.size() == 1
        books.findAll { it.bookType == BookType.NonFiction }.size() == 1
    }

    def "get book by multiple bookType InList"() {
        given:
        def params = [filter: [op: ['bookType': 'InList'], 'bookType': [BookType.Fiction.toString(), BookType.NonFiction.toString()].toArray()]]
        Book.findOrSaveWhere(bookType: BookType.Fiction)
        Book.findOrSaveWhere(bookType: BookType.Reference)
        Book.findOrSaveWhere(bookType: BookType.NonFiction)

        when:
        List<Book> books = (List<Book>) filterPaneService.filter(params, Book)

        then:
        Book.list().size() == 3
        books.size() == 2
        books.findAll { it.bookType == BookType.Fiction }.size() == 1
        books.findAll { it.bookType == BookType.Reference }.size() == 0
        books.findAll { it.bookType == BookType.NonFiction }.size() == 1
    }

    def "get book by multiple bookType NotInList"() {
        given:
        def params = [filter: [op: ['bookType': 'NotInList'], 'bookType': [BookType.Fiction.toString(), BookType.NonFiction.toString()].toArray()]]
        Book.findOrSaveWhere(bookType: BookType.Fiction)
        Book.findOrSaveWhere(bookType: BookType.Reference)
        Book.findOrSaveWhere(bookType: BookType.NonFiction)

        when:
        List<Book> books = (List<Book>) filterPaneService.filter(params, Book)

        then:
        Book.list().size() == 3
        books.size() == 1
        books[0].bookType == BookType.Reference
    }

    void cleanupSpec() {
//        Book.withSession {
//            Book.executeUpdate('delete Book')
//            Author.executeUpdate('delete Author')
//        }
    }

    private void createBookWithAuthors() {
        Book book = new Book(title: 'i like turtles', coAuthor: new Author(firstName: 'Co', lastName: 'Author'))
        book.authors = []
        book.authors << new Author(firstName: 'Cool', lastName: 'Dude')
        book.authors << new Author(firstName: 'Another', lastName: 'Dude')
        book.save(failOnError: true, flush: true)
        new Book(title: 'think about it').save(flush: true)
    }
}
