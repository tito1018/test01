package com.tito.svn.b.po;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class SubmitLogInfo {
	private String id;
	private String author;  //提交人
	private String message; //提交信息
	private long revision;  //svn 上的版本号
	private Date submitDate; //提交的时间
	private long sumCodeLine = 0l; //本次提交的有效代码行数（删除和空白行不计入，新增、注释和修改计入）
	
	private List<LogItemInfo> logItems = null;
	
	public void addLogItem(LogItemInfo item)
	{
		if(null == logItems)
		{
			logItems = new ArrayList<LogItemInfo>();
		}
		logItems.add(item);
	}
}
