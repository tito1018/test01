package com.tito.svn.a;


import lombok.Data;

/**
 * Created by youzongxu on 2018/9/10.
 */
@Data
public class SvnDataAuxiliaryTwo //extends BaseEntity<Long>
{
    private Long svnReversion;
    private Integer svnAddlines;
    private String svnFilepath;
    private String svnChangetype;
    
    public String toString()
    {
    	return " SvnDataAuxiliaryTwo.svnAddlines: " +svnAddlines + "," + svnFilepath + ", " + svnChangetype;
    }
}
