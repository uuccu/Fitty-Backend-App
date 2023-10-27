package fittibackendapp.common.security.configuration

import fittibackendapp.common.security.bean.JwtAccessDeniedHandler
import fittibackendapp.common.security.bean.JwtAuthenticationEntryPoint
import fittibackendapp.common.security.bean.JwtFilter
import fittibackendapp.common.security.component.CustomPasswordEncoder
import fittibackendapp.common.security.component.JwtVerifier
import fittibackendapp.common.security.type.AuthorizeRequestsApplier
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.AdviceMode
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    securedEnabled = true,
    prePostEnabled = true,
    mode = AdviceMode.PROXY,
)
class SecurityConfiguration(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Qualifier(AUTHORIZE_REQUEST_APPLIER_BEAN_NAME)
    private val authorizeRequestsApplier: AuthorizeRequestsApplier,
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private val jwtVerifier: JwtVerifier,
) {
    @Bean
    fun passwordEncoder() = CustomPasswordEncoder()

    @Bean
    fun jwtFilter(): JwtFilter = JwtFilter(jwtVerifier)

    @Bean
    fun authenticationEntryPoint(): AuthenticationEntryPoint = JwtAuthenticationEntryPoint()

    @Bean
    fun accessDeniedHandler(): AccessDeniedHandler = JwtAccessDeniedHandler()

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http.cors {

        }.csrf {
            it.disable()
        }.exceptionHandling {
            it.authenticationEntryPoint(authenticationEntryPoint())
            it.accessDeniedHandler(accessDeniedHandler())
        }.sessionManagement {
            it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        }.authorizeHttpRequests {
            it.shouldFilterAllDispatcherTypes(false)
        }.apply(authorizeRequestsApplier) // .oauth2Login() todo
            // .successHandler()
            // .userInfoEndpoint()
            // .userService()
            // .addFilterBefore(
            //     jwtFilter(),
            //     UsernamePasswordAuthenticationFilter::class.java,
            // )
            .build()
    }

    companion object {
        const val AUTHORIZE_REQUEST_APPLIER_BEAN_NAME = "authorizeRequestsApplier"
    }
}