package com.tito.svn.a;

import java.util.List;

import lombok.Data;

@Data
public class ChangeFile {

	private String filePath;
	private char  changeType;
	private String  body;
	private String   fileContent;
	
	
	public ChangeFile(String filePath, char changeType, String body) {
		super();
		this.filePath = filePath;
		this.changeType = changeType;
		this.body = body;
	}
	
	
}
