package sk.knet.dp.authorization

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
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


    @GetMapping("/addUser")
    @ResponseBody
    fun addUser(
            @RequestParam("username") username: String,
            @RequestParam("password") password: String,
            @RequestParam("roles") roles: String
    ): UserDetails? {

        val rolesList = roles.substringAfter("[").substringBeforeLast("]").split(", ")


        if (userDetailStore.manager.userExists(username)) {
            userDetailStore.manager.deleteUser(username)
        }

        val userBuilder = userDetailStore.users.username(username)
                .password(password)

        userBuilder.roles(*rolesList.toTypedArray())

        userDetailStore.manager.createUser(userBuilder.build())


        return userDetailStore.manager.loadUserByUsername(username)
    }

}
