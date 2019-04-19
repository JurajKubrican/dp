package sk.knet.dp.generator

import java.nio.file.Files
import java.nio.file.Paths

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.streams.toList

@Service
class FileStorage {

    val rootLocation: Path = Paths.get("filestorage")

    fun store(file: MultipartFile, fileName: String) {

        Files.copy(file.inputStream, this.rootLocation.resolve(fileName), StandardCopyOption.REPLACE_EXISTING)
    }

    fun listDir(): List<Path>? {
        return Files.list(rootLocation).toList()
    }

    fun delete(fileName: String) {
        Files.delete(this.rootLocation.resolve(fileName))
    }
}
