package sk.knet.dp.authorization

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.security.Principal

@EnableResourceServer
@Controller
class UserResource {

    @Autowired
    lateinit var userDetailStore: UserDetailStore

    @GetMapping("/user")
    @ResponseBody
    fun getUser(user: Principal): Principal {
        return user
    }


    @PostMapping("/user")
    @ResponseBody
    fun postUser(
            @RequestParam("client") clientName: String,
            @RequestParam("role") role: String,
            @RequestParam("pass") pass: String
    ) {

        userDetailStore.manager.createUser(
                userDetailStore.users.username(clientName + "_" + role)
                        .password(pass)
                        .roles(clientName + "_" + role)
                        .build())
        print(userDetailStore.manager)
    }

}
