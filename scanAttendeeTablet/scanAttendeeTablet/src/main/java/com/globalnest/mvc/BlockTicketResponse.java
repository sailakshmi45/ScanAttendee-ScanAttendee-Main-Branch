//  ScanAttendee Android
//  Created by Ajay on 12-Feb-2015
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.mvc;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Mayank Mishra
 *
 */
public class BlockTicketResponse implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    
    public String tickesAvilable="",ItemId="";
    
    public ArrayList<BlockTicketListController> ticketsList = new ArrayList<BlockTicketListController>();

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "BlockTicketResponse [tickesAvilable=" + tickesAvilable
		+ ", ItemId=" + ItemId + ", ticketsList=" + ticketsList + "]";
    }
    
    
    
    
    
}
