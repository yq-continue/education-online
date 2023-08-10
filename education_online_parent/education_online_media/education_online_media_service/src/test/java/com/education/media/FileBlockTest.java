package com.education.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author yang
 * @create 2023-08-03 10:46
 */
@SpringBootTest(classes = FileBlockTest.class)
public class FileBlockTest {

    //文件分块测试
    @Test
    public void FileBlock() throws Exception {
        //源文件
        File source = new File("C:\\Users\\yq\\Desktop\\test\\1.mp4");
        //将文件分块到此地
        File Block = new File("C:\\Users\\yq\\Desktop\\test\\block");
        //设置分块大小
        long blockSize = 1024 * 1024 * 5;
        //计算分块文件数量
        long countOfBlock = (long)Math.ceil(source.length() * 1.0 / blockSize);
        //文件分块
        RandomAccessFile r = new RandomAccessFile(source, "r");
        byte[] buffer = new byte[1024];
        for (long i = 0; i < countOfBlock; i++) {
            File fileOfBlock = new File(Block.getPath() + "\\" + i);
            RandomAccessFile rw = new RandomAccessFile(fileOfBlock, "rw");
            int len = -1;
            while ((len = r.read(buffer)) != -1){
                rw.write(buffer,0,len);
                if (fileOfBlock.length() >= blockSize){
                    break;
                }
            }
            rw.close();
        }
        r.close();
    }

    //文件合并测试
    @Test
    public void FileMerge() throws Exception {
        //合并文件所在地
        File Block = new File("C:\\Users\\yq\\Desktop\\test\\block");
        //源文件，用于测试
        File source = new File("C:\\Users\\yq\\Desktop\\test\\1.mp4");
        //将文件合并在哪个文件
        File target = new File("C:\\Users\\yq\\Desktop\\test\\2.mp4");
        //获取需要合并的文件
        File[] files = Block.listFiles();
        List<File> filesOfList = Arrays.asList(files);
        Collections.sort(filesOfList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                long sub = Long.parseLong(o1.getName()) - Long.parseLong(o2.getName());
                return (int)sub;
            }
        });
        //合并文件
        RandomAccessFile rw = new RandomAccessFile(target, "rw");
        byte[] buffer = new byte[1024];
        for (int i = 0; i < files.length; i++) {
            File merge = new File(Block.getPath() + "\\" + i);
            RandomAccessFile r = new RandomAccessFile(merge, "r");
            int len = -1;
            while ((len = r.read(buffer)) != -1){
                rw.write(buffer,0,len);
            }
            r.close();
        }
        rw.close();
        //测试是否合并成功
        FileInputStream fileInputStream = new FileInputStream(source);
        FileInputStream mergeFileStream = new FileInputStream(target);
        String md5OfSource = DigestUtils.md5Hex(fileInputStream);
        String md5OfTarget = DigestUtils.md5Hex(mergeFileStream);
        if (md5OfSource.equals(md5OfTarget)){
            System.out.println("合并成功");
        }

    }

}
