package com.gingytech.gtm.app.hybrid;

import java.io.Serializable;

public class GtTemplate implements Serializable {
	/**
	 * auto generated serialVersionUID
	 */
	private static final long serialVersionUID = -8615925988796501416L;
	
	private int id;
	private byte[] template;
	
	public GtTemplate() {
		this.id = -1;
		this.template = null;
	}
	
	public GtTemplate(int id, byte[] template) {
		this.id = id;
		this.template = template;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setTemplate(byte[] template) {
		this.template = template;
	}
	
	public byte[] getTemplate() {
		return this.template;
	}
}
