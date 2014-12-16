/**
 * 
 */
package org.fao.geonet.kernel.security.ecas;

import java.util.LinkedList;
import java.util.List;

import jeeves.resources.dbms.Dbms;
import jeeves.server.ProfileManager;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.Log;
import jeeves.utils.SerialFactory;

import org.fao.geonet.constants.Geonet;
import org.jdom.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.context.ContextLoader;

import eu.cec.digit.ecas.client.validation.DetailedAuthenticationSuccess;
import eu.cec.digit.ecas.client.validation.ExtraGroupHandlingException;

public class AddAuthenticatedRole
		extends
		eu.cec.digit.ecas.client.validation.AbstractUserDetailsExtraGroupHandler
		implements
		eu.cec.digit.ecas.client.validation.UserDetailsExtraGroupHandlerIntf,
		java.io.Serializable {

	private static final long serialVersionUID = -4360207962508499319L;

	@Override
	public List<String> getGroups(DetailedAuthenticationSuccess arg0)
			throws ExtraGroupHandlingException {
		List<String> list = new LinkedList<String>();
		ApplicationContext ctx = ContextLoader
				.getCurrentWebApplicationContext();

		list.add("authenticated");

		Dbms dbms = null;
		// Make sure the user exists on the database:
		try {
			dbms = (Dbms) ctx.getBean(ResourceManager.class).open(
					Geonet.Res.MAIN_DB);
			String query = "SELECT id FROM Users WHERE username = ?";
			Element users = dbms.select(query, arg0.getUid());
			if (users.getChildren().isEmpty()) {
				query = "INSERT INTO Users (id, username, password, surname, name, profile, "
						+ "address, city, state, zip, country, email, organisation, authtype) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

				SerialFactory sf = new SerialFactory();
				String id = sf.getSerial(dbms, "Users") + "";

				dbms.execute(query, Integer.valueOf(id), arg0.getUid(), "",
						arg0.getLastName(), arg0.getFirstName(),
						ProfileManager.GUEST, "", "", "", "", "",
						arg0.getEmail(), arg0.getOrgId(), "ECAS");
			}

			// Extract roles, groups and profiles
			Element dbUserProfilRequest = dbms
					.select("SELECT profile FROM Users WHERE username=?",
							arg0.getUid());
			if (dbUserProfilRequest.getChild("record") != null) {
				String dbUserProfil = dbUserProfilRequest.getChild("record")
						.getChildText("profile");
				list.add(dbUserProfil);
			}

		} catch (Exception e) {
			throw new ExtraGroupHandlingException(
					"Could not connect to local database", e);
		} finally {
			try {
				ctx.getBean(ResourceManager.class).close(Geonet.Res.MAIN_DB,
						dbms);
			} catch (Exception e) {
				throw new ExtraGroupHandlingException(
						"Could not connect to local database", e);
			}
		}

		return list;
	}

}