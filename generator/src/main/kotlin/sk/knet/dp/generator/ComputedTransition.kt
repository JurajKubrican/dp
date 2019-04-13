package sk.knet.dp.generator

import sk.knet.dp.petriflow.DataRef
import sk.knet.dp.petriflow.I18NStringType
import sk.knet.dp.petriflow.RoleRef
import sk.knet.dp.petriflow.Transition

class ComputedTransition(t: Transition) : Transition() {

    var rolesView: Set<String> = emptySet()
    var rolesPerform: Set<String> = emptySet()
    var data: List<DataRef> = emptyList()

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
                .toList()

    }

}
