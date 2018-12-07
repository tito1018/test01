package com.tito.svn;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;  
  
public class SVNUtilTest {  
    private static String url = "https://wg772aifvgqhkx6/svn/TestCompManagement/SourceCode/Develop/branch/LogAnalysis";  
    private static SVNRepository repository = null;  
  
//    @BeforeClass  
    public static void setupLibrary() {  
        DAVRepositoryFactory.setup();  
        SVNRepositoryFactoryImpl.setup();  
        FSRepositoryFactory.setup();  
        try {  
            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));  
        }  
        catch (SVNException e) {  
//            logger.error(e.getErrorMessage(), e);  
        	e.printStackTrace();
        }  
        // 身份验证  
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager("huangtt","huangtt");  
        repository.setAuthenticationManager(authManager);  
    }  
  
//    @Test  
    public static void main(String[] args) throws Exception {  
    	setupLibrary();
    	
        // 过滤条件  
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");  
        final Date begin = format.parse("2018-08-13");  
        final Date end = format.parse("2018-12-14");  
        final String author = "";  
        long startRevision = 0;  
        long endRevision = -1;//表示最后一个版本  
        final List<String> history = new ArrayList<String>();  
        //String[] 为过滤的文件路径前缀，为空表示不进行过滤  
        repository.log(new String[]{""},  
                       startRevision,  
                       endRevision,  
                       true,  
                       true,  
                       new ISVNLogEntryHandler() {  
//                           @Override  
                           public void handleLogEntry(SVNLogEntry svnlogentry)   throws SVNException {  
                        	   
                        	   
                        	   String ab = svnlogentry.getAuthor() + "," + svnlogentry.getMessage() + "," ;
                        	   Map<String ,SVNLogEntryPath> map = svnlogentry.getChangedPaths();
                        	   System.out.println("==================="+ab);
                        	   for(Map.Entry<String, SVNLogEntryPath> item : map.entrySet())
                        	   {
                        		   System.out.println(item.getKey());
                        		   System.out.println(item.getValue());
                        		   System.out.println(item.getValue().getType());
                        		   System.out.println(item.getValue().getKind());
                        	   }
                        	   System.out.println("========end==========="+ab);
                        	   //依据提交时间进行过滤  
                               if (svnlogentry.getDate().after(begin)  
                                   && svnlogentry.getDate().before(end)) {  
                                   // 依据提交人过滤  
                                   if (!"".equals(author)) {  
                                       if (author.equals(svnlogentry.getAuthor())) {  
                                           fillResult(svnlogentry);  
                                       }  
                                   } else {  
                                       fillResult(svnlogentry);  
                                   }  
                               }  
                           }  
  
                           public void fillResult(SVNLogEntry svnlogentry) {  
                          //getChangedPaths为提交的历史记录MAP key为文件名，value为文件详情  
                               history.addAll(svnlogentry.getChangedPaths().keySet());  
                           }  
                       });  
//        for (String path : history) {  
//            System.out.println(path);  
//        }  
    }  
}  
