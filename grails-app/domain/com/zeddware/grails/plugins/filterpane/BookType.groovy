package com.zeddware.grails.plugins.filterpane
/**
 *
 * @author steve.krenek
 */
enum BookType {
    
    Fiction('F', 'Fiction'), NonFiction('NF', 'Non Fiction'), Reference('R', 'Reference')

    BookType(String id, String display) {
        this.id = id;
        this.display = display;
    }
    private final String display;
    final String id
    public String getDisplay() { return this.display; }
}

