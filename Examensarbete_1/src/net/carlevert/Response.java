package net.carlevert;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect
public class Response {
	
	@JsonProperty
	private Long id;
	
	@JsonProperty
	private Date timestamp;
	
	@JsonProperty
	private boolean valid;
	
	public Response() {
		this.valid = false;
		this.timestamp = new Date();
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	public boolean isValid() {
		return valid;
	}
}
