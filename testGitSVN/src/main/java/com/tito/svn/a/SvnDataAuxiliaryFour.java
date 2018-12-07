package com.tito.svn.a;

import lombok.Data;

@Data
public class SvnDataAuxiliaryFour {

	private long svnVersion;
    private String svnPath;
    private String svnType;
    
    public String toString()
    {
    	return " SvnDataAuxiliaryFour: " +svnVersion + "," + svnPath + ", " + svnType;
    }
}
