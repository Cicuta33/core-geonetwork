/**
 * 
 */
package org.fao.geonet.kernel.security.ecas;

import java.security.Principal;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * estat-geonetwork-main
 * 
 * @author delawen
 * 
 * 
 */
public class MyUserDetailsBuilderServiceImpl implements UserDetailsService {
	@Override
	public UserDetails loadUserByUsername(String username) {
		try {
			List<GrantedAuthority> userGroups = new LinkedList<GrantedAuthority>();
			String userId = "33"; // TODO retrieve the userId
			if (existsClass("weblogic.security.Security")) {
				Subject subject = (Subject) Class
						.forName("weblogic.security.Security")
						.getMethod("getCurrentSubject", null)
						.invoke(null, null);
				Set<Principal> principals = subject.getPrincipals();
				for (Principal principal : principals) {
					if (Class.forName("weblogic.security.spi.WLSGroup")
							.isInstance(principal)) {
						userGroups.add(new SimpleGrantedAuthority(principal
								.getName()));
					}
				}
				// TODO in the principals we can obtain all the data of the
				// user:
				/*
				 * Set<WLSUser> wlsUsers = subject.getPrincipals(WLSUser.class);
				 * if (!wlsUsers.isEmpty()) { WLSUser user =
				 * wlsUsers.iterator().next(); String uid = user.getName();
				 */
			}
			if (userId == null) {
				throw new UsernameNotFoundException(
						"Cannot retrieve the userDetails");
			}
			return new User(userId, null, true, true, true, true, userGroups);
		} catch (Exception ex) {
			if (ex instanceof UsernameNotFoundException) {
				throw (UsernameNotFoundException) ex;
			} else
				throw new UsernameNotFoundException(
						"Cannot retrieve the userDetails", ex);
		}
	}

	public boolean existsClass(String claszzName) {
		boolean exists = true;
		try {
			Class.forName(claszzName, false, getClass().getClassLoader());
		} catch (ClassNotFoundException e) {
			exists = false;
		}
		return exists;
	}
}
