package sk.knet.dp.authorization

import org.springframework.security.core.userdetails.User
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.stereotype.Component


@Component
class UserDetailStore {

    val manager = InMemoryUserDetailsManager()
    val users = User.withDefaultPasswordEncoder()

}
