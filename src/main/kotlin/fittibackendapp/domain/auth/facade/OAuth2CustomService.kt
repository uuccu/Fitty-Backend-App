package fittibackendapp.domain.auth.facade

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import fittibackendapp.domain.auth.entity.LoginType
import fittibackendapp.domain.auth.entity.Role
import fittibackendapp.domain.auth.entity.User
import fittibackendapp.domain.auth.repository.LoginTypeRepository
import fittibackendapp.domain.auth.repository.RoleRepository
import fittibackendapp.domain.auth.repository.UserRepository
import fittibackendapp.exception.DuplicatedEmailException
import jakarta.transaction.Transactional
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Collections
import java.util.Date

@Service
class OAuth2CustomService(
    // private val authenticationService: AuthenticationService,
    // private val userService: UserService,
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val loginTypeRepository: LoginTypeRepository,
    private val tokenAlgorithm: Algorithm,
): OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val delegateUserService = DefaultOAuth2UserService()
        val oAuth2User = delegateUserService.loadUser(userRequest)

        val registrationId =
            userRequest.clientRegistration.registrationId.uppercase() // "google", "kakao", "facebook"...
        val userNameAttributeName =
            userRequest.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName

        val email = oAuth2User.attributes.get("email") as String
        val name = oAuth2User.attributes.get("name") as String

        var user = userRepository.findByEmail(email)


        if (user == null) {
            // val picture = oAuth2User.attributes.get("picture") as String
            val loginType = LoginType.GOOGLE // todo KAKAO, NAVER, FACEBOOK
            saveOrUpdate(
                email = email,
                name = name,
                oauth2LoginType = loginType,
            )
        }
        else {
            // todo n+1 but 로그인타입이 5가지뿐
            if (user.loginType.name != LoginType.GOOGLE) { // todo KAKAO, NAVER, FACEBOOK registrationId와 비교.
                throw DuplicatedEmailException()
            }
        }

        val token = JWT.create()
            .withClaim("email", email)
            .withClaim("password", "")
            .withClaim("role", "ROLE_USER")
            .withExpiresAt(
                Date.from(
                    LocalDateTime
                        .now()
                        .plusDays(1)
                        .atZone(ZoneId.of("Asia/Seoul"))
                        .toInstant(),
                ),
            )
            .sign(tokenAlgorithm)

        return DefaultOAuth2User(
            Collections.singleton(
                SimpleGrantedAuthority("ROLE_USER"),
            ),
            Collections.singletonMap("token", token) as Map<String, Any>?,
            "token",
        )
    }

    @Transactional
    fun saveOrUpdate(
        email: String,
        name: String,
        oauth2LoginType: String,
    ) {
        val role = roleRepository.findByName(Role.ROLE_USER) ?: throw RuntimeException("ROLE_USER가 없습니다.")
        val loginType =
            loginTypeRepository.findByName(oauth2LoginType) ?: throw RuntimeException("EMAIL Login Type이  없습니다.")

        val user = User(
            email = email,
            password = "",
            name = name,
            role = role,
            loginType = loginType,
        ).apply {
            userRepository.save(this)
        }
    }
}
