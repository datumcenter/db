package io.gallery.db.util;

import com.sun.management.OperatingSystemMXBean;
import io.gallery.db.bean.FileInfo;
import io.gallery.db.bean.SystemInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ClassUtils;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.RuntimeMXBean;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class SystemUtil {
    private static final Log logger = LogFactory.getLog(SystemUtil.class);
    private static Map<String, Double> progressUnzip = new ConcurrentHashMap<>();
    private static Map<String, Map> lanHosts = new ConcurrentHashMap<>();

    public static Map<String, Map> getLanHostMap() {
        return lanHosts;
    }

    public static List getLanHostList() {
        return lanHosts.values().stream().collect(Collectors.toList());
    }

    public static Map<String, Double> getProgressUnzip() {
        return progressUnzip;
    }

    public static Double getProgressUnzip(String path) {
        return progressUnzip.get(path);
    }

    public static void progressUnzipCLear() {
        progressUnzip.clear();
    }

    public static String execute(String cmd) {
        String result;
        try {
            logger.debug("运行命令：" + cmd);
            result = exec(Runtime.getRuntime().exec(cmd));
        } catch (Exception e) {
            result = cmd + " 执行异常：" + e.getMessage();
            e.printStackTrace();
        }
        return result;
    }

    public static String execute(String[] cmd) {
        String result;
        try {
            logger.debug("运行命令：" + JSON.toJSONString(cmd));
            result = exec(Runtime.getRuntime().exec(cmd));
        } catch (Exception e) {
            result = Arrays.toString(cmd) + " 执行异常：" + e.getMessage();
            e.printStackTrace();
        }
        logger.debug("命令运行结果：" + result);
        return result;
    }

    public static String execute(String cmd, String[] env, File dir) {
        String result;
        try {
            logger.debug("在目录[" + dir + "]运行命令：" + cmd);
            result = exec(Runtime.getRuntime().exec(cmd, env, dir));
        } catch (Exception e) {
            result = "执行异常：" + e.getMessage();
            e.printStackTrace();
        }
        logger.debug("命令运行结果：" + result);
        return result;
    }

    private static String exec(Process process) throws IOException {
        String result;
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("GBK")));
        process.getOutputStream().close();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        result = sb.toString();
        return result;
    }


    public static double getSystemCpuLoad() {
        OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        return osmxb.getSystemCpuLoad() * 100;

    }

    public static double getMemoryLoad() {
        OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        double value = osmxb.getFreePhysicalMemorySize() * 1.0 / osmxb.getTotalPhysicalMemorySize();
        return (1 - value) * 100;
    }

    private static HashMap SOFTWARE_PATH;
    private static int GPU_TRY_TIME = 0;

    public static String getSystemGpu() {
        StringBuilder stringBuffer = new StringBuilder();
        if (GPU_TRY_TIME <= 3) {
            try {
                Process process = null;
                HashMap dict = null;
                try {
                    if (SOFTWARE_PATH == null) {
                        dict = DB.selectOne("select * from v_db_dict where no='LWS' and name='software_path'");
                    } else {
                        dict = SOFTWARE_PATH;
                    }
                } catch (Exception e) {
                    SOFTWARE_PATH = null;
                    return "";
                }
                if (dict == null) {
                    return "";
                }
                String path = (String) dict.get("value");
                if (DBT.isNull(path)) {
                    return "";
                }
                if (isWindows()) {
                    process = Runtime.getRuntime().exec(path);
                } else {
                    String[] shell = {"/bin/bash", "-c", path};
                    process = Runtime.getRuntime().exec(shell);
                }
                process.getOutputStream().close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = "";
                while (true) {
                    try {
                        if (null == (line = reader.readLine())) {
                            break;
                        }
                    } catch (IOException e) {
                        logger.debug("获取结果信息失败：" + e.getMessage());
                        GPU_TRY_TIME++;
                        SOFTWARE_PATH = null;
                    }
                    stringBuffer.append(line).append("\n");
                }
                SOFTWARE_PATH = dict;
            } catch (IOException e) {
                SOFTWARE_PATH = null;
                GPU_TRY_TIME++;
            }
        }
        return stringBuffer.toString();
    }

    public static List<Map> getTaskList() {
        List<Map> list = new ArrayList<>();
        String result = "";
        if (isWindows()) {
            result = execute("tasklist /nh /fo csv");
            for (String line : result.replaceAll("\",\"", "|").replaceAll("\"", "").split("\n")) {
                String[] infos = line.split("\\|");
                list.add(new LinkedHashMap() {{
                    put("name", infos[0]);
                    put("pid", infos[1]);
                    put("ppid", infos[2]);
                    put("sessionid", infos[3]);
                    put("memory", infos[4]);
                }});
            }
        } else {
            String[] shell = {"/bin/bash", "-c", "ps -ef"};
            result = execute(shell);
            for (String line : result.split("\n")) {
                String[] infos = line.split(",");
                list.add(new LinkedHashMap() {{
                    put("uid", infos[0]);
                    put("pid", infos[1]);
                    put("ppid", infos[2]);
                    put("c", infos[3]);
                    put("stime", infos[4]);
                    put("tty", infos[5]);
                    put("time", infos[6]);
                    put("cmd", infos[7]);
                }});
            }
        }
        return list;
    }

    public static List<Map> getGpus() {
        List<Map> result = new ArrayList<>();
        try {
            String gpus = getSystemGpu();//命令行调用后获取的信息
            if (DBT.isNull(gpus)) {
                return result;
            }
            /*gpus = "Mon Nov 15 23:10:04 2021\n" +
                    "+-----------------------------------------------------------------------------+\n" +
                    "| NVIDIA-SMI 456.38       Driver Version: 456.38       CUDA Version: 11.1     |\n" +
                    "|-------------------------------+----------------------+----------------------+\n" +
                    "| GPU  Name            TCC/WDDM | Bus-Id        Disp.A | Volatile Uncorr. ECC |\n" +
                    "| Fan  Temp  Perf  Pwr:Usage/Cap|         Memory-Usage | GPU-Util  Compute M. |\n" +
                    "|===============================+======================+======================|\n" +
                    "|   0  GeForce GTX 1070   WDDM  | 00000000:01:00.0  On |                  N/A |\n" +
                    "| 47%   31C    P8    10W / 160W |    322MiB /  8192MiB |      0%      Default |\n" +
                    "+-------------------------------+----------------------+----------------------+\n" +
                    "\n" +
                    "+-----------------------------------------------------------------------------+\n" +
                    "| Processes:                                                                  |\n" +
                    "|  GPU   GI   CI        PID   Type   Process name                  GPU Memory |\n" +
                    "|        ID   ID                                                   Usage      |\n" +
                    "|=============================================================================|\n" +
                    "|    0   N/A  N/A      1244    C+G   Insufficient Permissions        N/A      |\n" +
                    "|    0   N/A  N/A      1272    C+G   Insufficient Permissions        N/A      |\n" +
                    "|    0   N/A  N/A      6592    C+G   Insufficient Permissions        N/A      |\n" +
                    "|    0   N/A  N/A      7768    C+G   C:\\Windows\\explorer.exe         N/A      |\n" +
                    "|    0   N/A  N/A     11552    C+G   ...w5n1h2txyewy\\SearchUI.exe    N/A      |\n" +
                    "|    0   N/A  N/A     12036    C+G   ...y\\ShellExperienceHost.exe    N/A      |\n" +
                    "|    0   N/A  N/A     12464    C+G   ...me\\Application\\chrome.exe    N/A      |\n" +
                    "+-----------------------------------------------------------------------------+";*/
            String[] split = gpus.split("\\|===============================\\+======================\\+======================\\|");
            String[] gpusInfos = split[1].split(" {79}");
            String[] gpuInfo = gpusInfos[0].split("\\+-------------------------------\\+----------------------\\+----------------------\\+"); // 分割多个gpu
            for (int i = 0; i < gpuInfo.length - 1; i++) {
                Map info = new LinkedHashMap();
                String[] nameAndInfo = gpusInfos[i].split("\n");
                //只要第二块的数据 0 TITAN V Off
                String[] split1 = nameAndInfo[1].split("\\|")[1].split("\\s+");//去空格 // 0  TITAN V             Off
                info.put("index", split1[1]);
                StringBuilder name = new StringBuilder();
                for (int j = 0; j < split1.length - 1; j++) {
                    if (j > 1) {
                        name.append(split1[j]).append(" ");
                    }
                }
                info.put("name", name.toString());
                String[] basic = nameAndInfo[2].split("\\|")[1].split("\\s+");
                info.put("fan", basic[1]);
                info.put("temp", basic[2]);
                info.put("perf", basic[3]);
                String[] memory = nameAndInfo[2].split("\\|")[2].split("\\s+");
                info.put("usedMemory", memory[1]);
                info.put("totalMemory", memory[3]);
                String[] other = nameAndInfo[2].split("\\|")[3].split("\\s+");
                info.put("util", other[1]);
                info.put("mode", other[2]);
                result.add(info);
            }
        } catch (Exception e) {
            logger.error("处理显卡结果信息失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取操作系统信息
     *
     * @return OperatingSystemMXBean
     */
    public static OperatingSystemMXBean getOperatingSystemMXBean() {
        return (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    /**
     * 获取JVM运行时信息
     *
     * @return RuntimeMXBean
     */
    public static RuntimeMXBean getRuntimeMXBean() {
        return ManagementFactory.getRuntimeMXBean();
    }

    /**
     * 获取JVM的内存
     *
     * @return MemoryMXBean
     */
    public static MemoryMXBean getMemoryMXBean() {
        return ManagementFactory.getMemoryMXBean();
    }

    /**
     * 获取JVM的内存池情况
     *
     * @return List
     */
    public static List<MemoryPoolMXBean> getMemoryPoolMXBeans() {
        return new ArrayList(ManagementFactory.getMemoryPoolMXBeans());
    }

    public static Map getRuntime() {
        Runtime runtime = Runtime.getRuntime();
        return new HashMap() {{
            put("freeMemory", runtime.freeMemory());
            put("totalMemory", runtime.totalMemory());
            put("maxMemory", runtime.maxMemory());
        }};
    }

    public static int getThreads() {
        ThreadGroup parentThread;
        for (parentThread = Thread.currentThread().getThreadGroup(); parentThread.getParent() != null; parentThread = parentThread.getParent()) {
        }
        return parentThread.activeCount();
    }

    public static List<FileInfo> listRoots() {
        List<FileInfo> result = new ArrayList<>();
        for (File root : File.listRoots()) {
            result.add(getFileInfo(root));
        }
        return result;
    }

    public static List<FileInfo> listFiles(File path) {
        List<FileInfo> result = new ArrayList<>();
        if (path == null) {
            return result;
        }
        if (path.listFiles() == null) {
            return result;
        }
        for (File file : path.listFiles()) {
            result.add(getFileInfo(file));
        }
        return result.stream().sorted(Comparator.comparing(FileInfo::isFile).thenComparing(FileInfo::getLastModified)).collect(Collectors.toList());
    }

    public static FileInfo getFileInfo(File file) {
        return Optional.ofNullable(file).map(f -> new FileInfo() {{
            setOriginal(f);
            setPath(f.getPath());
            setAbsoluteFile(f.getAbsolutePath());
            setAbsolutePath(f.getAbsolutePath());
            setLastModified(f.lastModified());
            setTotalSpace(f.getTotalSpace());
            setFreeSpace(f.getFreeSpace());
            setUsableSpace(f.getUsableSpace());
            setLength(f.length());
            setAbsolute(f.isAbsolute());
            setDirectory(f.isDirectory());
            setHidden(f.isHidden());
            setFile(f.isFile());
            setParent(f.getParent());
            setName(f.getName());
        }}).orElse(null);

    }

    public static SystemInfo getSystemInfo() {
        return new SystemInfo() {{
            setOsname(System.getProperty("os.name"));
            setOsversion(System.getProperty("os.version"));
            setOsarch(System.getProperty("os.arch"));
            setJavahome(System.getProperty("java.home"));
            setJavaversion(System.getProperty("java.version"));
            setCpus(Runtime.getRuntime().availableProcessors());
            setPid(System.getProperty("PID"));
            setUsername(System.getProperty("user.name"));
            setUserhome(System.getProperty("user.home"));
            setUserdir(System.getProperty("user.dir"));
            setStarttime(ManagementFactory.getRuntimeMXBean().getStartTime());
        }};
    }

    public static boolean isWindows() {
        return getSystemInfo().getOsname().toLowerCase().contains("windows");
    }

    public static String getRuntimeDir() {
        String path = ClassUtils.getDefaultClassLoader().getResource("").getPath();
        String result;
        if (DBT.isNotNull(path) && path.contains(".jar")) {//jar方式运行
            String jarPath = DBT.subString(path, 0, new File(path).getPath().indexOf(".jar") + 4);
            result = jarPath.substring(0, jarPath.lastIndexOf("/") + 1).replaceFirst("file:", "");
        } else {//开发方式运行
            result = System.getProperty("user.dir") + File.separator;
        }
        if (isWindows() && result.startsWith("/")) {//例如：/D:
            result = result.replaceFirst("/", "");
        }
        return result;
    }

    public static String readFile(String path) {
        StringBuilder text = null;
        File file = new File(path);
        try {
            if (!file.exists() || file.isDirectory()) {
                logger.error("readFile fail:【" + path + "】文件不存在或是文件夹");
            } else {
                text = new StringBuilder();
                InputStreamReader in = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(in);
                String line;
                while ((line = reader.readLine()) != null) {
                    text.append("\n").append(line);
                }
                in.close();
                reader.close();
            }
        } catch (IOException e) {
            logger.error("readFile fail:" + e.getMessage());
        }
        return text == null ? null : text.toString();
    }

    /**
     * 获取源文件的编码
     *
     * @param filePath 源文件所在的绝对路径
     * @return
     */
    private static String getFileEnCode(String filePath) {
        InputStream inputStream = null;
        String code = "";
        try {
            inputStream = new FileInputStream(filePath);
            byte[] head = new byte[3];
            inputStream.read(head);
            code = "gb2312";
            if (head[0] == -1 && head[1] == -2) {
                code = "UTF-16";
            }
            if (head[0] == -2 && head[1] == -1) {
                code = "Unicode";
            } else if (head[0] == -27 && head[1] == -101 && head[2] == -98) {
                code = "UTF-8"; //UTF-8(不含BOM)
            } else if (head[0] == -17 && head[1] == -69 && head[2] == -65) {
                code = "UTF-8"; //UTF-8-BOM
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return code;
    }

    /**
     * 递归删除文件夹
     *
     * @param path 要被删除的文件或者目录
     * @return 删除成功返回true, 否则返回false
     */
    public static boolean deleteFile(File path) {
        if (path == null) {
            return false;
        }
        if (!path.exists()) {// 如果dir对应的文件不存在，则退出
            return false;
        }
        if (path.isFile()) {
            return path.delete();
        } else {
            for (File file : path.listFiles()) {
                deleteFile(file);
            }
        }
        return path.delete();
    }

    /**
     * 获得指定文件的byte数组
     *
     * @param filePath String
     * @return byte[]
     */
    public static byte[] getBytes(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (IOException e) {
            logger.error("获得指定文件的byte数组异常：" + e.getMessage());
        }
        return buffer;
    }

    /**
     * 通过response输出文件
     *
     * @param response HttpServletResponse
     * @param fileName String
     * @param bs       byte[]
     */
    public static void responseWrite(HttpServletResponse response, String fileName, byte[] bs) {
        response.reset();
        response.setContentType("application/octet-stream;charset=UTF-8");
        try {
            response.setHeader("filename", URLEncoder.encode(fileName, "UTF-8"));
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
            OutputStream os = response.getOutputStream();
            if (bs != null) {
                os.write(bs);
                os.close();
            } else {
                response.sendRedirect("404");
            }
        } catch (IOException e) {
            logger.error("输出文件异常：" + e.getMessage());
        }
    }

    /**
     * 通过response输出文件
     *
     * @param response HttpServletResponse
     * @param fileName String
     * @param bs       byte[]
     */
    public static void responseWritePic(HttpServletResponse response, String fileName, byte[] bs) {
        response.reset();
        response.setContentType("image/jpeg");
        try {
            OutputStream os = response.getOutputStream();
            if (bs != null) {
                os.write(bs);
                os.close();
            } else {
                response.setStatus(404);
            }
        } catch (IOException e) {
            logger.error("输出文件异常：" + e.getMessage());
        }
    }

    /**
     * 功能:压缩多个文件成一个zip文件
     *
     * @param srcfile：源文件列表
     * @param zipfile：压缩后的文件
     */
    public static void zipFiles(List<File> srcfile, File zipfile) {
        byte[] buf = new byte[1024];
        try {
            //ZipOutputStream类：完成文件或文件夹的压缩
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfile));
            for (File file : srcfile) {
                FileInputStream in = new FileInputStream(file);
                out.putNextEntry(new ZipEntry(file.getName()));
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.closeEntry();
                in.close();
            }
            out.close();
        } catch (Exception e) {
            logger.error("压缩文件异常：" + e.getMessage());
        }
    }

    /**
     * 压缩方法
     * （可以压缩空的子目录）
     *
     * @param srcPath     压缩源路径
     * @param zipFileName 目标压缩文件
     * @return boolean
     */
    public static boolean zip(String srcPath, String zipFileName) {
        File srcFile = new File(srcPath);
        List<File> fileList = getAllFiles(srcFile);// 扫描所有要压缩的文件
        byte[] buffer = new byte[512];// 缓冲器
        ZipEntry zipEntry = null;
        int readLength = 0;// 每次读出来的长度
        try {
            ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFileName));
            for (File file : fileList) {
                if (file.isFile()) {// 若是文件，则压缩这个文件
                    zipEntry = new ZipEntry(getRelativePath(srcPath, file));
                    zipEntry.setSize(file.length());
                    zipEntry.setTime(file.lastModified());
                    zipOutputStream.putNextEntry(zipEntry);
                    InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                    while ((readLength = inputStream.read(buffer, 0, 512)) != -1) {
                        zipOutputStream.write(buffer, 0, readLength);
                    }
                    inputStream.close();
                } else {
                    zipEntry = new ZipEntry(getRelativePath(srcPath, file) + "/");//若是目录（即空目录）则将这个目录写入zip条目
                    zipOutputStream.putNextEntry(zipEntry);
                }
            }
            zipOutputStream.close();
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 解压缩方法
     *
     * @param zipFilePath 压缩文件名
     * @param targetPath  解压目标路径
     * @return boolean
     */
    public static boolean unzip(String zipFilePath, String targetPath) {
        Double progress = progressUnzip.get(zipFilePath);
        if (progress != null) {//正在解压中
            return true;
        }
        progressUnzip.put(zipFilePath, 0d);
        logger.debug("解压缩开始，压缩文件名：" + zipFilePath + "，解压目标路径：" + targetPath);
        long start = System.currentTimeMillis();
        try {
            long totalSize = new File(zipFilePath).length();// 总大小
            long readSize = 0;
            File dirTarget = new File(targetPath);
            if (dirTarget.exists()) {
                dirTarget.renameTo(new File(targetPath + "_" + DBT.format(System.currentTimeMillis(), "yyyyMMddHHmmssSSS") + "_bak"));
            }
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath), Charset.forName("GBK"));
            ZipEntry zipEntry = null;
            byte[] buffer = new byte[512];
            int readLength = 0;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.isDirectory()) {//若是zip条目目录，则需创建这个目录
                    File dir = new File(targetPath + File.separator + zipEntry.getName());
                    if (!dir.exists()) {
                        dir.mkdirs();
                        continue;
                    }
                }
                File file = createUnZipFile(targetPath, zipEntry.getName());//若是文件，则需创建该文件
                OutputStream outputStream = new FileOutputStream(file);
                while ((readLength = zipInputStream.read(buffer, 0, 512)) != -1) {
                    outputStream.write(buffer, 0, readLength);
                }
                readSize += zipEntry.getCompressedSize();// 累加字节长度
                progressUnzip.put(zipFilePath, (readSize * 1.0 / totalSize) * 100);
                outputStream.close();
            }
            zipInputStream.close();
        } catch (Exception e) {
            logger.error("解压缩失败：" + e.getMessage(), e);
            progressUnzip.remove(zipFilePath);
            logger.error("删除解压缩失败文件夹：" + targetPath + "：" + new File(targetPath).delete());
            return false;
        }
        logger.debug("解压缩成功：" + targetPath + "耗时：" + (System.currentTimeMillis() - start) + "毫秒");
        progressUnzip.put(zipFilePath, 100d);
        progressUnzip.remove(zipFilePath);
        return true;
    }

    /**
     * 取的给定源目录下的所有文件及空的子目录
     * 递归实现
     *
     * @param file File
     * @return List
     */
    private static List<File> getAllFiles(File file) {
        List<File> fileList = new ArrayList<>();
        File[] tmp = file.listFiles();
        for (File value : tmp) {
            if (value != null) {
                if (value.isFile()) {
                    fileList.add(value);
                }
                if (value.isDirectory()) {
                    if (value.listFiles().length != 0) {// 若不是空目录，则递归添加其下的目录和文件
                        fileList.addAll(getAllFiles(value));
                    } else {
                        fileList.add(value);// 若是空目录，则添加这个目录到fileList
                    }
                }
            }
        }
        return fileList;
    }

    /**
     * 取相对路径
     * 依据文件名和压缩源路径得到文件在压缩源路径下的相对路径
     *
     * @param dirPath 压缩源路径
     * @param file    File
     * @return 相对路径
     */
    private static String getRelativePath(String dirPath, File file) {
        File dir = new File(dirPath);
        String relativePath = file.getName();
        StringBuilder sb = new StringBuilder(relativePath);
        while (true) {
            file = file.getParentFile();
            if (file == null) {
                break;
            }
            if (file.equals(dir)) {
                break;
            } else {
                sb.append(file.getName()).append("/").append(relativePath);
            }
        }
        return sb.toString();
    }

    /**
     * 创建文件
     * 根据压缩包内文件名和解压缩目的路径，创建解压缩目标文件，
     * 生成中间目录
     *
     * @param unZipPath 解压缩目的路径
     * @param fileName  压缩包内文件名
     * @return 解压缩目标文件
     */
    private static File createUnZipFile(String unZipPath, String fileName) {
        String[] dirs = fileName.split("/");  // 将文件名的各级目录分解
        File file = new File(unZipPath);
        if (dirs.length > 1) {// 文件有上级目录
            for (int i = 0; i < dirs.length - 1; i++) {
                file = new File(file, dirs[i]);// 依次创建文件对象直到文件的上一级目录
            }
            if (!file.exists()) {
                file.mkdirs();// 文件对应目录若不存在，则创建
            }
            file = new File(file, dirs[dirs.length - 1]); // 创建文件
        } else {
            if (!file.exists()) {
                file.mkdirs();//若目标路径的目录不存在，则创建
            }
            file = new File(file, dirs[0]); //创建文件
        }
        return file;
    }

    public static boolean renameFile(String oldFilePath, String newName) {
        try {
            File oldFile = new File(oldFilePath);
            if (oldFile.exists()) {//若文件存在
                //判断是全路径还是文件名
                if (!newName.contains("/") && !newName.contains("\\")) {
                    //单文件名，判断是windows还是Linux系统
                    String absolutePath = oldFile.getAbsolutePath();
                    if (newName.indexOf("/") > 0) {
                        newName = absolutePath.substring(0, absolutePath.lastIndexOf("/") + 1) + newName;//Linux系统
                    } else {
                        newName = absolutePath.substring(0, absolutePath.lastIndexOf("\\") + 1) + newName;
                    }
                }
                File file = new File(newName);
                //判断重命名后的文件是否存在
                if (file.exists()) {
                    logger.error("该文件已存在,不能重命名");
                } else {
                    //不存在，重命名
                    return oldFile.renameTo(file);
                }
            } else {
                logger.error("原该文件不存在,不能重命名");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getIpLocal() {
        String ip = null;
        try {
            ip = Inet4Address.getLocalHost().getHostAddress();
        } catch (Exception e) {
            logger.error("IP地址获取失败" + e.getMessage(), e);
        }
        return ip;
    }

    public static String getMacAddress() {
        InetAddress ia = null;
        try {
            ia = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        byte[] mac = new byte[0];
        try {
            mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
        } catch (Exception e) {
            logger.debug("未获取到MAC地址");
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            if (i != 0) {
                sb.append("-");
            }
            //字节转换为整数
            int temp = mac[i] & 0xff;
            // 把无符号整数参数所表示的值转换成以十六进制表示的字符串
            String str = Integer.toHexString(temp);
            if (str.length() == 1) {
                sb.append("0").append(str);
            } else {
                sb.append(str);
            }
        }
        return sb.toString().toUpperCase();
    }

    public static Map getIpsLocal(Class type) {
        Map<String, Map> map = new HashMap<>();
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (inetAddress != null && (type == null || inetAddress.getClass().equals(type))
                            && !inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) {
                        map.put(inetAddress.getHostAddress(), new HashMap() {{
                            put("networkInterface", networkInterface);
                            put("isSiteLocalAddress", inetAddress.isSiteLocalAddress());
                            put("isLoopbackAddress", inetAddress.isLoopbackAddress());
                            put("canonicalHostName", inetAddress.getCanonicalHostName());
                            put("isAnyLocalAddress", inetAddress.isAnyLocalAddress());
                            put("isLinkLocalAddress", inetAddress.isLinkLocalAddress());
                            put("isMCLinkLocal", inetAddress.isMCLinkLocal());
                            put("isMCGlobal", inetAddress.isMCGlobal());
                            put("isMCNodeLocal", inetAddress.isMCNodeLocal());
                            put("isMCOrgLocal", inetAddress.isMCOrgLocal());
                            put("isMCSiteLocal", inetAddress.isMCSiteLocal());
                            put("isMulticastAddress", inetAddress.isMulticastAddress());
                        }});
                    }
                }
            }
        } catch (Exception e) {
            logger.error("IP地址获取失败" + e.getMessage(), e);
        }
        return map;
    }

    public static Map getIpV4sLocal() {
        return getIpsLocal(Inet4Address.class);
    }

    public static Map getIpV6sLocal() {
        return getIpsLocal(Inet6Address.class);
    }

    public static Map getInfo() {
        oshi.SystemInfo si = new oshi.SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();
        Map infoMemory = new HashMap();
        Map infoCpu = new HashMap();
        Map infoDisk = new HashMap();
        FileSystem fileSystem = os.getFileSystem();
        try {
            if (fileSystem != null) {
                List<OSFileStore> fileStores = fileSystem.getFileStores();
                List<Map> disks = new ArrayList<>();
                long total = 0L;
                long free = 0L;
                for (OSFileStore fileStore : fileStores) {
                    Map disk = Maps.init();
                    disk.put("name", fileStore.getName());
                    disk.put("mount", fileStore.getMount());
                    total += fileStore.getTotalSpace();
                    disk.put("total", formatByte(fileStore.getTotalSpace()));
                    free += fileStore.getFreeSpace();
                    disk.put("free", formatByte(fileStore.getFreeSpace()));
                    disk.put("usuage", new DecimalFormat("###.##").format((fileStore.getTotalSpace() - fileStore.getFreeSpace()) * 100.0 / fileStore.getTotalSpace()));
                    disks.add(disk);
                }
                infoDisk.put("disks", disks);
                infoDisk.put("total", formatByte(total));
                infoDisk.put("free", formatByte(free));
                infoDisk.put("usuage", new DecimalFormat("###.##").format((total - free) * 100.0 / total));
            }
            GlobalMemory memory = si.getHardware().getMemory();
            long totalByte = memory.getTotal();
            long freeByte = memory.getAvailable();
            infoMemory.put("total", formatByte(totalByte));
            infoMemory.put("free", formatByte(freeByte));
            infoMemory.put("used", formatByte(totalByte - freeByte));
            infoMemory.put("usuage", new DecimalFormat("###.##").format((totalByte - freeByte) * 100.0 / totalByte));

            CentralProcessor processor = si.getHardware().getProcessor();
            long[] prevTicks = processor.getSystemCpuLoadTicks();
            TimeUnit.SECONDS.sleep(1);
            long[] ticks = processor.getSystemCpuLoadTicks();
            long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
            long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
            long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
            long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
            long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
            long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
            long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
            long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
            long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;
            infoCpu.put("count", processor.getLogicalProcessorCount());
            //cpu当前使用率
            infoCpu.put("usuage", new DecimalFormat("###.##").format(100.0 - (idle * 1.0 / totalCpu) * 100.0));
            //cpu系统使用率
            infoCpu.put("usuageSystem", new DecimalFormat("###.##").format(cSys * 100.0 / totalCpu));
            //cpu用户使用率
            infoCpu.put("usuageUser", new DecimalFormat("###.##").format(user * 100.0 / totalCpu));
            //cpu当前等待率
            infoCpu.put("usuageWait", new DecimalFormat("###.##").format(iowait * 100.0 / totalCpu));
        } catch (Exception ignored) {

        }
        return Maps.init()
                .put("computerSystem", hal.getComputerSystem())
                .put("memory", hal.getMemory())
                .put("sensors", hal.getSensors())
                .put("fileSystem", fileSystem)
                .put("networkIFs", hal.getNetworkIFs())
                .put("networkParams", os.getNetworkParams())
                .put("displays", hal.getDisplays())
                .put("graphicsCards", hal.getGraphicsCards())
                .put("infoMemory", infoMemory)
                .put("infoCpu", infoCpu)
                .put("infoDisk", infoDisk)
                ;
    }

    /**
     * 字节容量换算
     *
     * @param byteNumber long
     * @return String
     */
    public static String formatByte(long byteNumber) {
        double FORMAT = 1024.0;
        double kbNumber = byteNumber / FORMAT;
        if (kbNumber < FORMAT) {
            return new DecimalFormat("#.###KB").format(kbNumber);
        }
        double mbNumber = kbNumber / FORMAT;
        if (mbNumber < FORMAT) {
            return new DecimalFormat("#.###MB").format(mbNumber);
        }
        double gbNumber = mbNumber / FORMAT;
        if (gbNumber < FORMAT) {
            return new DecimalFormat("#.###GB").format(gbNumber);
        }
        double tbNumber = gbNumber / FORMAT;
        return new DecimalFormat("#.###TB").format(tbNumber);
    }

    /**
     * 局域网唤醒
     *
     * @param macAddress String
     * @param ipAddress  String
     * @return boolean
     */
    public static boolean wakeOnLan(String macAddress, String ipAddress) {
        boolean result = false;
        StringBuilder stringBuilder = new StringBuilder("FF:FF:FF:FF:FF:FF");
        if (DBT.isNotNull(macAddress) && macAddress.contains("-")) {
            macAddress = macAddress.replaceAll("-", ":");
        }
        for (int i = 0; i < 16; i++) {
            stringBuilder.append(":");
            stringBuilder.append(macAddress);
        }

        try {
            byte[] bytes = getHexBytes(stringBuilder.toString());
            InetAddress address = InetAddress.getByName(ipAddress);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 9);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();
            result = true;
            logger.debug("已发送局域网唤醒数据包");
        } catch (Exception e) {
            logger.error("局域网唤醒失败:" + e.getMessage());
        }
        return result;
    }

    public static byte[] getHexBytes(String input) {
        byte[] bytes = null;
        try {
            logger.debug(input);
            String[] hex = input.split("(\\:|\\-)");
            bytes = new byte[hex.length];
            for (int i = 0; i < hex.length; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (NumberFormatException e) {
            bytes = null;
            logger.error("请使用十六进制的字符：" + e.getMessage());
        }
        return bytes;
    }

    /**
     * 获取局域网所有主机ip和Mac，并异步获取主机名
     *
     * @return Map
     */
    public static Map<String, String> getLanIpAndMacs() {
        String ipLocal = getIpLocal();
        String macLocal = getMacAddress();
        Map<String, String> result = Maps.init(ipLocal, macLocal);
        String execute = execute(isWindows() ? "arp -a" : "arp -n");
        if (DBT.isNotNull(execute)) {
            String ipMac = execute;
            if (isWindows() && execute.contains(ipLocal)) {
                ipMac = execute.substring(execute.indexOf(ipLocal));
                ipMac = ipMac.substring(0, ipMac.indexOf(":"));
            }
            String[] ipMacs = ipMac.split("\n");
            for (String im : ipMacs) {
                String ip = DBT.getByPattern(im, "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");
                String mac = DBT.getByPattern(im, "([A-Fa-f0-9]{2}" + (isWindows() ? "-" : ":") + "){5}[A-Fa-f0-9]{2}");
                if (DBT.isNotNull(ip) && DBT.isNotNull(mac)) {
                    result.put(ip, mac.toUpperCase());
                }
            }
            getLanHostnames(result);
        }
        return result;
    }

    /**
     * 获取主机名
     *
     * @param ipMacs Map
     */
    public static void getLanHostnames(Map<String, String> ipMacs) {
        ThreadPoolExecutor executor =
                new ThreadPoolExecutor(5, 50, 300, TimeUnit.MILLISECONDS,
                        new ArrayBlockingQueue<>(3), new ThreadPoolExecutor.CallerRunsPolicy());
        for (String ip : ipMacs.keySet()) {
            executor.execute(() -> {
                try {
                    String command = "ping -a " + ip + " -w 300 -n 1";
                    if (!SystemUtil.isWindows()) {
                        command = "ping " + ip + " -c 1";
                    }
                    Runtime r = Runtime.getRuntime();
                    Process p;
                    p = r.exec(command);
                    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.forName("GBK")));
                    String inline;
                    while ((inline = br.readLine()) != null) {
                        if (inline.contains("[")) {
                            int start = inline.indexOf("Ping ");
                            int end = inline.indexOf("[");
                            String hostname = inline.substring(start + "Ping ".length(), end - 1);
                            logger.debug(ip + "主机名：" + hostname);
                            lanHosts.put(ip, Maps.init("mac", ipMacs.get(ip)).put("hostname", hostname).put("ip", ip));
                        }
                    }
                    br.close();
                } catch (IOException e) {
                    logger.error("依据IP获取主机名失败：" + e.getMessage());
                }
            });
        }
    }

    /**
     * 检查给定端口是否可用
     *
     * @param ip   String
     * @param port int
     * @return boolean
     */
    public static boolean isPortEnable(String ip, int port) {
        try {
            new Socket(InetAddress.getByName(ip), port);
            return true;
        } catch (Exception e) {
            // 异常说明端口连接不上
        }
        return false;
    }
}
