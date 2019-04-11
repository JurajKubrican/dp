package sk.knet.dp.authorization

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.security.Principal

@EnableResourceServer
@Controller
class UserResource{

    @Autowired
    lateinit var userDetailStore: UserDetailStore

    @GetMapping("/user" )
    @ResponseBody
    fun getUser(user: Principal): Principal {
        return user
    }


    @PostMapping("/user")

    fun postUser(client: String, role: String, pass: String) {
        userDetailStore.manager.createUser(userDetailStore.users.username(role).password(pass).roles(client + "_" + role).build())
    }

}
