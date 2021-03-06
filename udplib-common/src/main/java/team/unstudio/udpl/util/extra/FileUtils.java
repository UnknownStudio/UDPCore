package team.unstudio.udpl.util.extra;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public interface FileUtils {
	
    /**
     * 读取文件内容到List
     *
     * @param file 文件
     * @param list 要写入到的List
     * @param code 编码
     */
    static void readFile2List(File file, List<String> list, String code) throws Exception {
        BufferedReader fr;
        try {
            String myCode = code!=null&&!"".equals(code) ? code : Charset.defaultCharset().name();
            InputStreamReader read = new InputStreamReader(new FileInputStream(
                    file), myCode);

            fr = new BufferedReader(read);
            String line;
            while ((line = fr.readLine()) != null && line.trim().length() > 0) {
                list.add(line);
            }
            fr.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void readFile2List(URL url, List<String> list, String code) throws Exception {
        URLConnection connection = url.openConnection();
        InputStream in = connection.getInputStream();
        InputStreamReader read = new InputStreamReader(in, code);

        BufferedReader fr = new BufferedReader(read);
        String line;
        while ((line = fr.readLine()) != null && line.trim().length() > 0) {
            list.add(line);
        }

        in.close();
    }

    /**
     * 读取文件内容到数组
     *
     * @param file 文件
     * @return 文件内容分行的数组
     * @param code 编码
     */
    static String[] readFile2Array(File file, String code) throws Exception {
        List<String> list = new ArrayList<>();
        readFile2List(file, list, code);
        return list.toArray(new String[0]);
    }

    static String[] readFile2Array(URL url, String code) throws Exception {
        List<String> list = new ArrayList<>();
        readFile2List(url, list, code);
        return list.toArray(new String[0]);
    }

    static void writeArray2File(File file, Object[] objects, String code) {
        try {
            String myCode = code != null && !"".equals(code) ? code : Charset.defaultCharset().name();
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), myCode);
            BufferedWriter writer = new BufferedWriter(out);
            for (int i = 0; i < objects.length; i++) {
                writer.write(objects[i].toString());
                if (i < objects.length - 1) writer.newLine();
            }

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
