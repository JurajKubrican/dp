package sk.knet.dp.generator

import sk.knet.dp.petriflow.Document
import sk.knet.dp.petriflow.Role
import java.io.File
import javax.xml.bind.JAXBContext

class NetReader(f: String) {

    var facadeTransitions: List<FacadeTransition>
    var roles: List<Role>
    var document: Document

    init {
        document = loadFile(f)


        facadeTransitions = document.transition
                .map { FacadeTransition(it, document.data) }
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
