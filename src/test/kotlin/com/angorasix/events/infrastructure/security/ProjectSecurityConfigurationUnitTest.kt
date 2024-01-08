package com.angorasix.events.infrastructure.security

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
class ProjectSecurityConfigurationUnitTest {

    @Test
    fun `when security configuration is created - then Jwt config is used`() {
        val http = mockk<ServerHttpSecurity>()
        val slot = slot<Customizer<ServerHttpSecurity.AuthorizeExchangeSpec>>()
        every { http.authorizeExchange(capture(slot)) } returns http
        every { http.oauth2ResourceServer(any()) } returns http
        val securityFilterChain = mockk<SecurityWebFilterChain>()
        every { http.build() } returns securityFilterChain

        val resultingSecurityFilterChain =
            EventsSecurityConfiguration().springSecurityFilterChain(http)

        assertThat(resultingSecurityFilterChain).isEqualTo(securityFilterChain)

        verify { http.authorizeExchange(any()) }
        verify { http.oauth2ResourceServer(any()) }
        verify { http.build() }

        // assert inner config
        val capturedConfiguration = slot.captured
        val exchanges = mockk<ServerHttpSecurity.AuthorizeExchangeSpec>()
        val mainAccess = mockk<ServerHttpSecurity.AuthorizeExchangeSpec.Access>()
        every {
            exchanges.pathMatchers(
                HttpMethod.GET,
                "/projects-core/**",
            )
        } returns mainAccess
        every { mainAccess.permitAll() } returns exchanges
        val defaultAccess = mockk<ServerHttpSecurity.AuthorizeExchangeSpec.Access>()
        every { exchanges.anyExchange() } returns defaultAccess
        every { defaultAccess.authenticated() } returns exchanges
        capturedConfiguration.customize(exchanges)

        verify {
            exchanges.pathMatchers(
                HttpMethod.GET,
                "/projects-core/**",
            )
        }
        verify { mainAccess.permitAll() }
        verify { exchanges.anyExchange() }
        verify { defaultAccess.authenticated() }
    }
}
