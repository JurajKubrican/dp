package sk.knet.dp.generator

import sk.knet.dp.petriflow.*

class FacadeTransition(t: Transition, dataIn: List<Data>) : Transition() {

    var rolesView: Set<String> = emptySet()
    var rolesPerform: Set<String> = emptySet()
    var data: List<FacadeData> = emptyList()

    init {
        id = t.id

        val placeholderLabel = I18NStringType()
        placeholderLabel.value = "Transition$id"
        label = label ?: placeholderLabel


        rolesView = t.roleRef
                .filter { (it.logic.isView || it.logic.isPerform) }
                .map(RoleRef::getId)
                .toSet()

        rolesPerform = t.roleRef
                .filter { it.logic.isPerform!! }
                .map(RoleRef::getId)
                .toSet()


        data = t.dataGroup
                .map { it.dataRef }
                .filter { it != null }
                .flatten()
                .union(t.dataRef)
                .toList().map { dataRef ->
                    val dataField = dataIn.find { itt -> itt.id == dataRef.id }!!
                    FacadeData(dataField.id, dataRef.logic, dataField.type, dataField.values.map { it.value })
                }

    }

}
