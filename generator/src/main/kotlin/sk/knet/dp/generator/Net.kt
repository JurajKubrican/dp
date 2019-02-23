package sk.knet.dp.generator

import sk.knet.dp.petriflow.Document
import sk.knet.dp.petriflow.Role

class Net(d: Document) {

    var computedTransitions: List<ComputedTransition> = emptyList()
    var roles: Map<String, Role> = emptyMap()

    init {
        computedTransitions = d.transition
                .map { ComputedTransition(it) }
                .toList()
        roles = d.role
                .map { it.id to it }
                .toMap()
    }
}
