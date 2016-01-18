package org.grails.plugins.filterpane.test

enum FavoriteGenre {
    SciFi('Science Fiction'), Fantasy(), Romance(), Mystery(), Fiction(), Reference(), Satire()

    final String display

    private FavoriteGenre() {
        this.display = name()
    }

    private FavoriteGenre(String display) {
        this.display = display
    }
}
