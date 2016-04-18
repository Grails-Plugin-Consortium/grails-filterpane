package com.demo

/**
 * @author steve.krenek
 */
enum BookType {

    Fiction('F', 'Fiction'), NonFiction('NF', 'Non Fiction'), Reference('R', 'Reference')

    BookType(String id, String display) {
        this.id = id
        this.display = display
    }

    final String display
    final String id
}
