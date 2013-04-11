package com.linuxbox.enkive.authentication.ldap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.linuxbox.enkive.authentication.EnkiveRoles;

/**
 * Puts an Enkive User Details facade over other User Details, to which most
 * methods are delegated to. Enkive User Details adds a set of known email
 * addresses and the methods to manage that set.
 */
public class EnkiveUserDetails implements UserDetails {
	private static final long serialVersionUID = 3003366042873560086L;

	protected UserDetails delegate;

	/**
	 * Email addresses that this user is known by and therefore has access to
	 * emails sent from or received by.
	 */
	protected Set<String> knownEmailAddresses;
	protected boolean isAdmin = false;
	protected boolean isUser = false;

	public EnkiveUserDetails(UserDetails plainUser) {
		this.delegate = plainUser;
		knownEmailAddresses = new HashSet<String>();

		if (plainUser instanceof EnkiveUserDetails) {
			knownEmailAddresses
					.addAll(((EnkiveUserDetails) plainUser).knownEmailAddresses);
		}

		determineRoles(plainUser);
	}

	protected void determineRoles(UserDetails user) {
		for (GrantedAuthority authority : user.getAuthorities()) {
			final String authorityString = authority.getAuthority();

			if (authorityString.equals(EnkiveRoles.ROLE_ADMIN)) {
				isAdmin = true;
			} else if (authorityString.equals(EnkiveRoles.ROLE_USER)) {
				isUser = true;
			}
		}
	}

	public void setKnownEmailAddresses(Collection<String> addresses) {
		knownEmailAddresses.clear();
		knownEmailAddresses.addAll(addresses);
	}

	public void addKnownEmailAddresses(Collection<String> addresses) {
		knownEmailAddresses.addAll(addresses);
	}

	public void addKnownEmailAddress(String address) {
		knownEmailAddresses.add(address);
	}

	public Set<String> getKnownEmailAddresses() {
		return Collections.unmodifiableSet(knownEmailAddresses);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return delegate.getAuthorities();
	}

	@Override
	public String getPassword() {
		return delegate.getPassword();
	}

	@Override
	public String getUsername() {
		return delegate.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return delegate.isAccountNonExpired();
	}

	@Override
	public boolean isAccountNonLocked() {
		return delegate.isAccountNonLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return delegate.isCredentialsNonExpired();
	}

	@Override
	public boolean isEnabled() {
		return delegate.isEnabled();
	}
}