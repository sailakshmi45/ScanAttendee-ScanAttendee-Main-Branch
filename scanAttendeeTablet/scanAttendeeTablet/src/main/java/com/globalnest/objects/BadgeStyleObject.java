package com.globalnest.objects;

import java.io.Serializable;
import java.util.ArrayList;

public class BadgeStyleObject implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ArrayList<BadgeRecord> records=new ArrayList<BadgeRecord>();

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BadgeStyleObject [records=" + records + "]";
	}
	
	
	
}
