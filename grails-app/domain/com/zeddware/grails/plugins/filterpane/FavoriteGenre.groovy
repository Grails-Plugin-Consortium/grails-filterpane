package com.zeddware.grails.plugins.filterpane
enum FavoriteGenre {
    SciFi('Science Fiction'), Fantasy(), Romance(), Mystery(), Fiction(), Reference(), Satire();

    private String display;

    private FavoriteGenre() {
        display = name();
    }

    private FavoriteGenre(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }
}

