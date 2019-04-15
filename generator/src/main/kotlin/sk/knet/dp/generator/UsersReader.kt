package sk.knet.dp.generator


import sk.knet.dp.userschema.Document
import sk.knet.dp.userschema.User
import java.io.File
import javax.xml.bind.JAXBContext

class UsersReader(f: String) {

    var document: Document
    var users: List<User>

    init {
        document = loadFile(f)
        users = document.user
    }


    private fun loadFile(fileName: String): Document {
        val file = File(fileName)
        val jaxbContext = JAXBContext.newInstance(Document::class.java)

        val jaxbUnmarshaller = jaxbContext.createUnmarshaller()
        return jaxbUnmarshaller.unmarshal(file) as Document
    }
}
