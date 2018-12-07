package com.tito.svn.a;

//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.zkr.cxhjcxt.constants.SvnTimeConstants;
//import com.zkr.cxhjcxt.dto.ChangeFile;
//import com.zkr.cxhjcxt.dto.SvnDto;
//import com.zkr.cxhjcxt.model.*;
import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
//import org.springframework.util.CollectionUtils;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.*;
import java.util.*;

/**
 * svn获取
 *
 * @author youzongxu
 * @date 2018/9/7
 */
//@Slf4j
public class svnutil {
    private String userName = "";
    private String password = "";
    private String urlString = "";
    /**
     * 临时文件
     */
    private String tempDir = System.getProperty("java.io.tmpdir");
    private DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
    private Random random = new Random();

    private SVNRepository repos;
    private ISVNAuthenticationManager authManager;

    public svnutil(String u,String p,String url) {
        try {
            userName = u;
            password = p;
            urlString = url;
            init();
        } catch (SVNException e) {
            e.printStackTrace();
        }
    }

    public void init() throws SVNException{
    	System.out.println("开始加载");
        authManager = SVNWCUtil.createDefaultAuthenticationManager(new File(tempDir+"/auth"), userName, password.toCharArray());
        options.setDiffCommand("-x -w");
        repos = SVNRepositoryFactory.create(SVNURL
                .parseURIEncoded(urlString));
        repos.setAuthenticationManager(authManager);
        System.out.println("init completed");
    }

    /**
     * 统计一段时间内代码增加量
     * @param st
     * @param et
     * @return
     * @throws Exception
     */
    public List<SvnDto> staticticsCodeAddByTime(Date st, Date et) throws Exception{
        SVNLogEntry[] logs = getLogByTime(st, et);
        List<SvnDto> ls = new ArrayList<SvnDto>();
        if(logs.length > 0){
            for(SVNLogEntry log:logs){
                File logFile = getChangeLog(log.getRevision(), log.getRevision()-1);
                SvnDto svnDto = new SvnDto();
                svnDto.setAuthor(log.getAuthor());
                svnDto.setRevision(log.getRevision());
                
                String str = log.getMessage();
                if(str == null)
                {
//                	str = SvnTimeConstants.SvnMessageFlag;
                	str = "";
                }
                
//                svnDto.setSvnMessage(StringUtils.isNotBlank(log.getMessage())?log.getMessage():SvnTimeConstants.SvnMessageFlag);
                svnDto.setSvnMessage(str);
                svnDto.setSvnTime(log.getDate());
                ls.add(svnDto);
                List<SvnDataAuxiliaryFour> fourList = getChangeFileList(log.getRevision());
                List<SvnDataAuxiliaryTwo> twoList = staticticsCodeAdd(logFile, log.getRevision());
                if(!StringUtils.isEmpty(fourList)){
                    svnDto.setFourList(fourList);
                }
                if(!StringUtils.isEmpty(twoList)){
                    svnDto.setTwoList(twoList);
                }
            }
        }
        return ls;
    }
    

    /**获取某一版本有变动的文件路径
     * @param version
     * @return
     * @throws SVNException
     */
    public List<SvnDataAuxiliaryFour> getChangeFileList(long version) throws SVNException{
        final List<SvnDataAuxiliaryFour> ls = new ArrayList<SvnDataAuxiliaryFour>();
        SVNLogClient logClient = new SVNLogClient( authManager, options );
        SVNURL url = SVNURL.parseURIEncoded(urlString);
        String[] paths = { "." };
        SVNRevision pegRevision = SVNRevision.create( version );
        SVNRevision startRevision = SVNRevision.create( version );
        SVNRevision endRevision = SVNRevision.create( version );
        long limit = 9999L;
        ISVNLogEntryHandler handler = new ISVNLogEntryHandler() {
            /**
             * This method will process when doLog() is done
             */
//            @Override
            public void handleLogEntry( SVNLogEntry logEntry ) {
                Map<String, SVNLogEntryPath> maps = logEntry.getChangedPaths();
                Set<Map.Entry<String, SVNLogEntryPath>> entries = maps.entrySet();
                for(Map.Entry<String, SVNLogEntryPath> entry : entries){
                    SvnDataAuxiliaryFour svnDataAuxiliaryFour = new SvnDataAuxiliaryFour();
                    svnDataAuxiliaryFour.setSvnVersion(logEntry.getRevision());
                    svnDataAuxiliaryFour.setSvnPath(entry.getValue().getPath());
                    svnDataAuxiliaryFour.setSvnType(String.valueOf(entry.getValue().getType()));
                    ls.add(svnDataAuxiliaryFour);
                }
            }
        };
        try {
            logClient.doLog( url, paths, pegRevision, startRevision, endRevision, false, true, limit, handler );
        }
        catch ( SVNException e ) {
            System.out.println( "Error in doLog() " );
            e.printStackTrace();
        }
        return ls;
    }

    /**获取一段时间内，所有的commit记录
     * @param st	开始时间
     * @param et	结束时间
     * @return
     * @throws SVNException
     */
    public SVNLogEntry[] getLogByTime(Date st, Date et) throws SVNException{
        long startRevision = repos.getDatedRevision(st);
        long endRevision = repos.getDatedRevision(et);
        @SuppressWarnings("unchecked")
        Collection<SVNLogEntry> logEntries = repos.log(new String[]{""}, null,
                startRevision, endRevision, true, true);
        SVNLogEntry[] svnLogEntries = logEntries.toArray(new SVNLogEntry[0]);
        SVNLogEntry[] svnLogEntries1 = Arrays.copyOf(svnLogEntries, svnLogEntries.length - 1);
        return svnLogEntries1;
    }


    /**获取版本比较日志，并存入临时文件
     * @param startVersion
     * @param endVersion
     * @return
     */
    public File getChangeLog(long startVersion, long endVersion) {
        SVNDiffClient diffClient = new SVNDiffClient(authManager, options);
        diffClient.setGitDiffFormat(true);
        File tempLogFile;
        OutputStream outputStream = null;
        String svnDiffFile;
        do {
            svnDiffFile = tempDir + "/svn_diff_file_"+startVersion+"_"+endVersion+"_"+random.nextInt(10000)+".txt";
            tempLogFile = new File(svnDiffFile);
        } while (tempLogFile != null && tempLogFile.exists());
        try {
            tempLogFile.createNewFile();
            outputStream = new FileOutputStream(svnDiffFile);
            diffClient.doDiff(SVNURL.parseURIEncoded(urlString),
                    SVNRevision.create(startVersion),
                    SVNURL.parseURIEncoded(urlString),
                    SVNRevision.create(endVersion),
                    SVNDepth.UNKNOWN, true, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(outputStream!=null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return tempLogFile;
    }

    /**分析变更的代码，统计代码增量
     * @param file
     * @param revision
     * @return
     * @throws Exception
     */
    public List<SvnDataAuxiliaryTwo> staticticsCodeAdd(File file, long revision) throws Exception{
        System.out.println("开始统计修改代码行数");
        List<SvnDataAuxiliaryTwo> twoList = new ArrayList<SvnDataAuxiliaryTwo>();
        FileReader fileReader = new FileReader(file);
        BufferedReader in = new BufferedReader(fileReader);
        String line;
        StringBuffer buffer = new StringBuffer(1024);
        boolean start = false;
        while((line=in.readLine()) != null){
            if(line.startsWith("Index:")){
                if(start){
                    ChangeFile changeFile = parseChangeFile(buffer);
                    if(null!=changeFile){
                        String filePath = changeFile.getFilePath();
                        if(getBoolean(filePath)){
                            int oneSize = staticOneFileChange(changeFile);
                            SvnDataAuxiliaryTwo svnDataAuxiliaryTwo = new SvnDataAuxiliaryTwo();
                            svnDataAuxiliaryTwo.setSvnAddlines(oneSize);
                            svnDataAuxiliaryTwo.setSvnFilepath("/"+changeFile.getFilePath());
                            svnDataAuxiliaryTwo.setSvnReversion(revision);
                            twoList.add(svnDataAuxiliaryTwo);
                        }
                    }
                    buffer.setLength(0);
                }
                start = true;
            }
            buffer.append(line).append('\n');
        }
        if(buffer.length() > 0){
            ChangeFile changeFile = parseChangeFile(buffer);
            if(null!=changeFile){
                String filePath = changeFile.getFilePath();
                if(getBoolean(filePath)){
                    int oneSize = staticOneFileChange(changeFile);
                    SvnDataAuxiliaryTwo svnDataAuxiliaryTwo = new SvnDataAuxiliaryTwo();
                    svnDataAuxiliaryTwo.setSvnAddlines(oneSize);
                    svnDataAuxiliaryTwo.setSvnFilepath("/"+changeFile.getFilePath());
                    svnDataAuxiliaryTwo.setSvnReversion(revision);
                    twoList.add(svnDataAuxiliaryTwo);
                }
            }
        }
        in.close();
        fileReader.close();
        boolean deleteFile = file.delete();
        System.out.println("-----delete file-----"+deleteFile);
        return twoList;
    }

    public boolean getBoolean(String filePath){
        String[] k = {".java", ".html", ".css", ".js", ".jsp", ".properties",".xml",".json",".sql",".wxml",".wxss"};
        List<String> strings = Arrays.asList(k);
        boolean ba = false;
        c:for (String ls:strings) {
            if(filePath.contains(ls)){
                ba = true;
                break c;
            }
        }
        return ba;
    }

    /**统计单个文件的增加行数，（先通过过滤器，如文件后缀、文件路径等等），也可根据修改类型来统计等，这里只统计增加或者修改的文件
     * @param changeFile
     * @return
     */
    public int staticOneFileChange(ChangeFile changeFile){
        char changeType = changeFile.getChangeType();
        char A = 'A';
        char M = 'M';
        if(A == changeType){
            return countAddLine(changeFile.getFileContent());
        }else if(M == changeType){
            return countAddLine(changeFile.getFileContent());
        }
        return 0;
    }

    /**解析单个文件变更日志:A表示增加文件，M表示修改文件，D表示删除文件，U表示末知
     * @param str
     * @return
     */
    public ChangeFile parseChangeFile(StringBuffer str){
        int index = str.indexOf("\n@@");
        if(index > 0){
            String header = str.substring(0, index);
            String[] headers = header.split("\n");
            String filePath = "";
            if(StringUtils.isNotBlank(headers[0])){
                filePath = headers[0].substring(7);
            }
            char changeType = 'U';
            boolean oldExist = !headers[2].endsWith("(nonexistent)");
            boolean newExist = !headers[3].endsWith("(nonexistent)");
            if(oldExist && !newExist){
                changeType = 'D';
            }else if(!oldExist && newExist){
                changeType = 'A';
            }else if(oldExist && newExist){
                changeType = 'M';
            }
            int bodyIndex = str.indexOf("@@\n")+3;
            String body = str.substring(bodyIndex);
            if(StringUtils.isNotBlank(filePath)){
                ChangeFile changeFile = new ChangeFile(filePath, changeType, body);
                return changeFile;
            }
        }else{
            String[] headers = str.toString().split("\n");
            System.out.println("headers"+headers[0]);
            if(StringUtils.isNotBlank(headers[0])){
                String filePath = headers[0].substring(7);
                ChangeFile changeFile = new ChangeFile(filePath, 'U', null);
                return changeFile;
            }
        }
        return null;
    }


    /**通过比较日志，统计以+号开头的非空行
     * @param content
     * @return
     */
    public int countAddLine(String content){
        int sum = 0;
        if(content !=null){
            content = '\n' + content +'\n';
            char[] chars = content.toCharArray();
            int len = chars.length;
            //判断当前行是否以+号开头
            boolean startPlus = false;
            //判断当前行，是否为空行（忽略第一个字符为加号）
            boolean notSpace = false;
            for(int i=0;i<len;i++){
                char ch = chars[i];
                if(ch =='\n'){
                    //当当前行是+号开头，同时其它字符都不为空，则行数+1
                    if(startPlus && notSpace){
                        sum++;
                        notSpace = false;
                    }
                    //为下一行做准备，判断下一行是否以+头
                    if(i < len-1 && chars[i+1] == '+'){
                        startPlus = true;
                        //跳过下一个字符判断，因为已经判断了
                        i++;
                    }else{
                        startPlus = false;
                    }
                }else if(startPlus && ch > ' '){
                    //如果当前行以+开头才进行非空行判断
                    notSpace = true;
                }
            }
        }
        return sum;
    }


    /**
     * 获取提交次数
     * @param dt
     * @return
     */
    public int countSum(Date dt){
        try {
            Date now = new Date();
            SVNLogEntry[] logByTime = getLogByTime(now, dt);
            List<SVNLogEntry> svnLogEntries = Arrays.asList(logByTime);
            return svnLogEntries.size();
        } catch (SVNException e) {
            return 0;
        }
    }

    /**
     * 获取提交量
     * @param dt
     * @return
     */
    public int dmCountSum(Date dt){
        int sum = 0;
        try {
            Date now = new Date();
            SVNLogEntry[] logByTime = getLogByTime(now, dt);
            List<SVNLogEntry> svnLogEntries = Arrays.asList(logByTime);
            sum = 0;
            for (SVNLogEntry ls:svnLogEntries) {
                sum += ls.getChangedPaths().size();
            }
        } catch (SVNException e) {
            e.printStackTrace();
        }
        return sum;
    }


    /**
     * 获取：1：今天 2：近一周 3：近一个月的数据
     * @return
     */
    public Map<String,Object> commitCount(){
        LocalDate localDate = new DateTime().toLocalDate();
        LocalDate localDateSeven = new DateTime().minusDays(SvnTimeConstants.seven).toLocalDate();
        LocalDate localDateThirty = new DateTime().minusDays(SvnTimeConstants.Thirty).toLocalDate();
        int today = countSum(localDate.toDate());
        int sevenToday = countSum(localDateSeven.toDate());
        int thirtyToday = countSum(localDateThirty.toDate());
        Map<String,Object> map = new HashMap<String,Object>();
        map.put("today",today);
        map.put("sevenToday",sevenToday);
        map.put("thirtyToday",thirtyToday);
        return map;
    }

    public Map<String,Object> dmCount(){
        Map<String,Object> map =  new HashMap<String,Object>();
        //日期改成时间排列
        LocalDate now = new DateTime().toLocalDate();
        LocalDate one = new DateTime().minusDays(SvnTimeConstants.one).toLocalDate();
        LocalDate two = new DateTime().minusDays(SvnTimeConstants.two).toLocalDate();
        LocalDate three = new DateTime().minusDays(SvnTimeConstants.three).toLocalDate();
        LocalDate four = new DateTime().minusDays(SvnTimeConstants.four).toLocalDate();
        LocalDate five = new DateTime().minusDays(SvnTimeConstants.five).toLocalDate();
        LocalDate six = new DateTime().minusDays(SvnTimeConstants.six).toLocalDate();
        LocalDate seven = new DateTime().minusDays(SvnTimeConstants.seven).toLocalDate();

        int now_count = dmCountSum(now.toDate());
        int one_count = dmCountSum(one.toDate())-now_count;
        int two_count = dmCountSum(two.toDate())-one_count;
        int three_count = dmCountSum(three.toDate())-two_count;
        int four_count = dmCountSum(four.toDate())-three_count;
        int five_count = dmCountSum(five.toDate())-four_count;
        int six_count = dmCountSum(six.toDate())-five_count;
        int seven_count = dmCountSum(seven.toDate())-six_count;

        map.put("now",now_count);
        map.put("one",one_count);
        map.put("two",two_count);
        map.put("three",three_count);
        map.put("four",four_count);
        map.put("five",five_count);
        map.put("six",six_count);
        map.put("seven",seven_count);
        return map;
    }
}
