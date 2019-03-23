package sk.knet.dp.authorization

import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.security.Principal

@EnableResourceServer
@Controller
class UserResource{

    @RequestMapping("/user")
    @ResponseBody
    fun getUser(user: Principal): Principal {
        return user
    }

}
