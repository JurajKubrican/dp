package sk.knet.dp.generator

import java.nio.file.Files
import java.nio.file.Paths

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class FileStorageImpl : FileStorage {

    val rootLocation = Paths.get("filestorage")

    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    override fun store(file: MultipartFile): String {


        val newFilename = (1..20)
                .map { i -> kotlin.random.Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("");

        Files.copy(file.getInputStream(), this.rootLocation.resolve(newFilename))
        return newFilename
    }

    override fun delete(fileName: String) {
        Files.delete(this.rootLocation.resolve(fileName))
    }

    override fun init() {
        Files.createDirectory(rootLocation)
    }

}
