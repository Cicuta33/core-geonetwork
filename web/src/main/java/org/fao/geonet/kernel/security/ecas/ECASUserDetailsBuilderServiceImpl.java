/**
 * 
 */
package org.fao.geonet.kernel.security.ecas;

import java.security.Principal;
import java.util.LinkedList;
import java.util.List;

import jeeves.guiservices.session.JeevesUser;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ProfileManager;
import jeeves.server.resources.ResourceManager;

import org.fao.geonet.constants.Geonet;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import eu.cec.digit.ecas.client.j2ee.tomcat.EcasPrincipal;

/**
 * estat-geonetwork-main
 * 
 * @author delawen
 * 
 * 
 */
public class ECASUserDetailsBuilderServiceImpl implements UserDetailsService,
		ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Override
	public UserDetails loadUserByUsername(String username) {
		try {
			List<GrantedAuthority> userGroups = new LinkedList<GrantedAuthority>();
			String userId = null;
			ProfileManager profileManager = applicationContext
					.getBean(ProfileManager.class);

			JeevesUser user = new JeevesUser(profileManager);

			ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder
					.currentRequestAttributes();
			Principal principal = attr.getRequest().getUserPrincipal();

			if (principal instanceof EcasPrincipal) {

				EcasPrincipal p = (EcasPrincipal) principal;

				userId = p.getUid();
				user.setUsername(userId);
				user.setEmail(p.getEmail());
				user.setName(p.getFirstName());
				user.setSurname(p.getLastName());

				// add id
				String query = "SELECT id, profile FROM Users WHERE username = ?";
				Dbms dbms = (Dbms) applicationContext.getBean(
						ResourceManager.class).open(Geonet.Res.MAIN_DB);
				Element record = dbms.select(query, userId).getChild("record");
				if (record != null) {
					user.setId(record.getChildText("id"));
					user.setProfile(record.getChildText("profile"));
				}
				applicationContext.getBean(ResourceManager.class).close(Geonet.Res.MAIN_DB, dbms);

				for (String rol : p.getRoles()) {
					userGroups.add(new SimpleGrantedAuthority(rol));
				}
			} else {
				throw new RuntimeException(
						"This userDetailsService only works with Ecas users");
			}

			if (userId == null) {
				throw new UsernameNotFoundException(
						"Cannot retrieve the userDetails, user unknown. Bad ECAS response: "
								+ principal);
			}

			return user;
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

	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

}
