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


    @PostMapping("/val")
    @ResponseBody
    fun postUser(
            @RequestParam("client") client: String,
            @RequestParam("user") user: String,
            @RequestParam("pass") pass: String
    ): String {

        val roleName = "${client.toUpperCase()}_${user.toUpperCase()}"

        if (userDetailStore.manager.userExists(user)) {
            userDetailStore.manager.updatePassword(
                    userDetailStore.manager.loadUserByUsername(user),
                    pass
            )
        } else {
            userDetailStore.manager.createUser(
                    userDetailStore.users.username(user)
                            .password(pass)
                            .roles(roleName)
                            .build())

            print(userDetailStore.manager.loadUserByUsername(user))
        }

        return "ROLE_$roleName"
    }

}
