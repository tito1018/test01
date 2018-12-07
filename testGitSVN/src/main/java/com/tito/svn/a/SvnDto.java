package com.tito.svn.a;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by youzongxu on 2018/9/7.
 */
@Data
public class SvnDto {
    private int sum;
//    private List<SvnDataAuxiliaryTwo> lists = new ArrayList<SvnDataAuxiliaryTwo>();
    private long version;
    
    
    private String author;
    private long  revision;
    private String svnMessage;
    private java.util.Date svnTime;
    
    private List<SvnDataAuxiliaryFour> fourList = new ArrayList<SvnDataAuxiliaryFour>();
    private List<SvnDataAuxiliaryTwo> twoList = new ArrayList<SvnDataAuxiliaryTwo>();
    
    
    public String toString()
    {
    	String str =  " SvnDtos: " +author + "," + sum + ", " + svnMessage;
    	if(twoList != null && twoList.size() > 0)
    	{
    		for(SvnDataAuxiliaryTwo tow : twoList)
    		{
    			str += "{" + tow.getSvnAddlines() + "," + tow.getSvnFilepath()+ "}";
    		}
    	}
    	return str;
    }
}
