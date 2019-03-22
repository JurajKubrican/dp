package sk.knet.dp

import javax.persistence.*

@Entity
@Table(name = "User")
data class User(
        @Id
        @GeneratedValue
        val id: Int,

        val login: String

) {
        
}
