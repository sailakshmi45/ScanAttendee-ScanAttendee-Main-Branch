//  ScanAttendee Android
//  Created by Ajay on Jun 28, 2016
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 *
 */
package com.globalnest.mvc;

import com.globalnest.objects.RegistrationSettingsController;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author laxmanamurthy
 *
 */
public class ItemsListResponse implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public List<TicketListResponseHandler> Itemscls_infoList = new ArrayList<TicketListResponseHandler>();
	public List<SeminaAgenda> agndaInfo = new ArrayList<SeminaAgenda>();
	public List<SessionGroup> userSessions = new ArrayList<SessionGroup>();
	public ArrayList<RegistrationSettingsController> settingsForScanAttendee = new ArrayList<RegistrationSettingsController>();
	public TicketProfileController tktprofilefieldtype=new TicketProfileController();
}
