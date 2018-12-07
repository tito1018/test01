package com.tito.svn.b.po;

import lombok.Data;

@Data
public class LogItemInfo {
	private String id;
	private String path;
	private Character changeType;
	private long codeLine;
}
