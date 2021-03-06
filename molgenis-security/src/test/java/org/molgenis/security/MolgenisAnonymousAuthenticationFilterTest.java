package org.molgenis.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class MolgenisAnonymousAuthenticationFilterTest
{
	private static Authentication AUTHENTICATION;

	@BeforeClass
	public static void setUpBeforeClass()
	{
		AUTHENTICATION = SecurityContextHolder.getContext().getAuthentication();
	}

	@AfterClass
	public static void tearDownAfterClass()
	{
		SecurityContextHolder.getContext().setAuthentication(AUTHENTICATION);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void doFilter() throws IOException, ServletException
	{
		UserDetailsService userDetailsService = mock(UserDetailsService.class);
		UserDetails userDetails = mock(UserDetails.class);
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_SOMETHING");
		when((Collection<GrantedAuthority>) (userDetails.getAuthorities())).thenReturn(
				Arrays.<GrantedAuthority> asList(authority));
		when(userDetailsService.loadUserByUsername(SecurityUtils.ANONYMOUS_USERNAME)).thenReturn(userDetails);
		MolgenisAnonymousAuthenticationFilter filter = new MolgenisAnonymousAuthenticationFilter("key",
				SecurityUtils.ANONYMOUS_USERNAME, userDetailsService);
		ServletRequest uestreq = mock(HttpServletRequest.class);
		ServletResponse response = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);
		filter.doFilter(uestreq, response, chain);
		verify(chain).doFilter(uestreq, response);
		assertEquals(SecurityContextHolder.getContext().getAuthentication().getName(), SecurityUtils.ANONYMOUS_USERNAME);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void doFilter_currentUser() throws IOException, ServletException
	{
		Authentication authentication = mock(Authentication.class);
		when(authentication.getName()).thenReturn("user");

		SecurityContextHolder.getContext().setAuthentication(authentication);
		UserDetailsService userDetailsService = mock(UserDetailsService.class);
		UserDetails userDetails = mock(UserDetails.class);
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_SOMETHING");
		when((Collection<GrantedAuthority>) (userDetails.getAuthorities())).thenReturn(
				Arrays.<GrantedAuthority> asList(authority));
		when(userDetailsService.loadUserByUsername(SecurityUtils.ANONYMOUS_USERNAME)).thenReturn(userDetails);
		MolgenisAnonymousAuthenticationFilter filter = new MolgenisAnonymousAuthenticationFilter("key",
				SecurityUtils.ANONYMOUS_USERNAME, userDetailsService);
		ServletRequest uestreq = mock(HttpServletRequest.class);
		ServletResponse response = mock(HttpServletResponse.class);
		FilterChain chain = mock(FilterChain.class);
		filter.doFilter(uestreq, response, chain);
		verify(chain).doFilter(uestreq, response);
		assertEquals(SecurityContextHolder.getContext().getAuthentication().getName(), "user");
	}
}
