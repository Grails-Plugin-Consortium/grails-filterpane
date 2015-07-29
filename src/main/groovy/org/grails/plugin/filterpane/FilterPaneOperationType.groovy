package org.grails.plugin.filterpane

enum FilterPaneOperationType implements Serializable {

    ILike('ILike'),
    NotILike('NotILike'),
    Like('Like'),
    NotLike('NotLike'),
    Equal('Equal'),
    NotEqual('NotEqual'),
    IsNull('IsNull'),
    IsNotNull('IsNotNull'),
    LessThan('LessThan'),
    LessThanEquals('LessThanEquals'),
    GreaterThan('GreaterThan'),
    GreaterThanEquals('GreaterThanEquals'),
    Between('Between'),
    InList('InList'),
    NotInList('NotInList'),
    BeginsWith('BeginsWith'),
    IBeginsWith('IBeginsWith'),
    EndsWith('EndsWith'),
    IEndsWith('IEndsWith')

    String operation

    FilterPaneOperationType(String operation) {
        this.operation = operation
    }

    static FilterPaneOperationType getFilterPaneOperationType(String operation) {
        values().find { it.operation == operation }
    }

    String toString() {
        operation
    }
}
