package sk.knet.dp.generator

import sk.knet.dp.petriflow.Document
import sk.knet.dp.petriflow.Role
import java.io.File
import javax.xml.bind.JAXBContext

class Net(f: String) {

    var computedTransitions: List<ComputedTransition>
    var roles: List<Role>
    var document: Document

    init {
        document = loadFile(f)

        computedTransitions = document.transition
                .map { ComputedTransition(it) }
                .toList()

        roles = document.role
    }


    private fun loadFile(fileName: String): Document {
        val file = File(fileName)
        val jaxbContext = JAXBContext.newInstance(Document::class.java)

        val jaxbUnmarshaller = jaxbContext.createUnmarshaller()
        return jaxbUnmarshaller.unmarshal(file) as Document
    }
}
