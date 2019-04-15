package sk.knet.dp.authorization


import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.core.userdetails.UserDetailsService


@Configuration
class SecurityConfig : WebSecurityConfigurerAdapter() {

    override fun configure(web: WebSecurity) {
        web.ignoring().antMatchers("/addUser**")
    }

    @Autowired
    lateinit var userDetailStore: UserDetailStore


    @Bean
    public override fun userDetailsService(): UserDetailsService {

        userDetailStore.manager.createUser(userDetailStore.users.username("user").password("password").roles("USER").build())
        userDetailStore.manager.createUser(userDetailStore.users.username("admin").password("password").roles("USER", "ADMIN").build())

        return userDetailStore.manager

    }



}
