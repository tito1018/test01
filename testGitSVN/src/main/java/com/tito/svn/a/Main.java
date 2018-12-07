package com.tito.svn.a;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Main {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String url = "https://wg772aifvgqhkx6/svn/TestCompManagement/SourceCode/Develop/branch/LogAnalysis";
		String user = "huangtt";
		String pwd = "huangtt";
		svnutil util = new svnutil(user,pwd,url);

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");  
        Date st = format.parse("2018-08-13");  
        Date et = format.parse("2018-12-14");  
        
		
        List<SvnDto> list = util.staticticsCodeAddByTime(st, et);
        if(null != list && list.size() > 0)
        {
        	for(SvnDto dto : list)
        	{
        		System.out.println(dto);
        	}
        }
	}

}
