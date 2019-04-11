package sk.knet.dp.generator

import org.springframework.web.multipart.MultipartFile

interface FileStorage {
    fun store(file: MultipartFile): String
    fun init()
}
