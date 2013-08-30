//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.services.group;

import jeeves.constants.Jeeves;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.services.util.ServiceMetadataReindexer;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;


/**
 * Removes a group from the system. Note that the group MUST NOT have operations
 * associated.
 */
public class Remove extends NotInReadOnlyModeService {
	public void init(String appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception
	{
		String id = Util.getParam(params, Params.ID);

		Dbms dbms = (Dbms) context.getResourceManager().open (Geonet.Res.MAIN_DB);

        OperationAllowedRepository repository = context.getBean(OperationAllowedRepository.class);
                
        List<Element> reindex = new ArrayList<Element>();
		Integer iId = Integer.valueOf(id);
		List<OperationAllowed> operationsAllowed = repository.findById_GroupId(iId);
		for (OperationAllowed operationAllowed : operationsAllowed) {
		    Element record = new Element("record");
		    record.addContent(new Element("metadataid").setText(Integer.toString(operationAllowed.getId().getMetadataId())));
		    reindex.add(record);
            repository.delete(operationAllowed);
            dbms.execute("DELETE FROM UserGroups       WHERE groupId=?",iId);
            dbms.execute("DELETE FROM GroupsDes        WHERE idDes=?"  ,iId);
            dbms.execute("DELETE FROM Groups           WHERE id=?"     ,iId);
        }

		//--- reindex affected metadata

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager   dm = gc.getBean(DataManager.class);

		ServiceMetadataReindexer s = new ServiceMetadataReindexer(dm, dbms, reindex);
		s.process();

		return new Element(Jeeves.Elem.RESPONSE)
							.addContent(new Element(Jeeves.Elem.OPERATION).setText(Jeeves.Text.REMOVED));
	}
}