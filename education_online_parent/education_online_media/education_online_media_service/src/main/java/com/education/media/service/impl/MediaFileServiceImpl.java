package com.education.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.education.base.exception.EducationException;
import com.education.base.model.PageParams;
import com.education.base.model.PageResult;
import com.education.media.feiclient.ContentServiceClient;
import com.education.media.mapper.MediaFilesMapper;
import com.education.media.mapper.MediaProcessMapper;
import com.education.media.model.dto.QueryMediaParamsDto;
import com.education.media.model.dto.UploadFileParamsDto;
import com.education.media.model.dto.UploadFileResultDto;
import com.education.media.model.po.MediaFiles;
import com.education.media.model.po.MediaProcess;
import com.education.media.model.po.RestResponse;
import com.education.media.service.MediaFileService;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private ContentServiceClient contentServiceClient;

    @Value("${minio.bucket.files}")
    private String bucketOfFiles;

    @Value("${minio.bucket.videofiles}")
    private String bucketOfVedio;



    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {
        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(isFill(queryMediaParamsDto.getFilename()),MediaFiles::getFilename,queryMediaParamsDto.getFilename());
        queryWrapper.eq(isFill(queryMediaParamsDto.getFileType()),MediaFiles::getFileType,queryMediaParamsDto.getFileType());
        queryWrapper.eq(isFill(queryMediaParamsDto.getType()),MediaFiles::getFileType,queryMediaParamsDto.getType());
        queryWrapper.eq(isFill(queryMediaParamsDto.getAuditStatus()),MediaFiles::getAuditStatus,queryMediaParamsDto.getAuditStatus());
        if (pageParams.getPageNo() != null && pageParams.getPageSize() != null){
            //分页对象
            Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
            // 查询数据内容获得结果
            Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
            // 获取数据列表
            List<MediaFiles> list = pageResult.getRecords();
            // 获取数据总数
            long total = pageResult.getTotal();
            // 构建结果集
            PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
            return mediaListResult;
        }
        List<MediaFiles> mediaFiles = mediaFilesMapper.selectList(queryWrapper);
        PageResult<MediaFiles> pageResult = new PageResult();
        pageResult.setItems(mediaFiles);
        return pageResult;


    }
    @Transactional
    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath,String objectName) {
        // 将文件上传到 minio 中
        // 获取文件扩展名
        String filename = uploadFileParamsDto.getFilename();
        String extension = getExtension(filename);
        //获取 mimetype
        String mimeType = getMimeType(extension);
        // 拼装 objectName
        String defaultFolderPath = getDefaultFolderPath();
        String fileMd5 = getFileMd5(new File(localFilePath));
        if (StringUtils.isEmpty(objectName)){
            //若没有传输 objectName ，则默认按照年月日方式拼装存储路径
            objectName = defaultFolderPath + fileMd5 + extension;
        }
        //上传文件
        boolean success = uploadFileToMinio(mimeType, bucketOfFiles, localFilePath, objectName);
        if (!success){
            EducationException.cast("文件上传失败");
        }
        // 将数据保存到数据库中
        MediaFiles mediaFiles = addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucketOfFiles, objectName);
        //返回结果
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;
    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        // 查询数据库中是否存在此文件
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null){
            //查询minio中是否存在此文件
            try {
                GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                        .bucket(mediaFiles.getBucket()).object(mediaFiles.getFilePath()).build();
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                if (inputStream != null){
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                log.debug("传输视频检查文件错误");
            }
        }
        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        //拼装 object
        String object = fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/"
                + fileMd5 + "/chunk/" + chunkIndex;

        //查询 minio 中是否存在此文件
        FilterInputStream inputStream = null;
        try {
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(bucketOfVedio).object(object).build();
            inputStream = minioClient.getObject(getObjectArgs);
            if (inputStream != null){
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            log.debug("传输视频检查分块文件错误");
        }
        return RestResponse.success(false);
    }

    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {
        String mimeType = getMimeType(null);
        String object = fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/"
                + fileMd5 + "/chunk/" + chunk;
        boolean success = uploadFileToMinio(mimeType, bucketOfVedio, localChunkFilePath, object);
        if (!success) {
            log.debug("上传分块文件失败:{}", object);
            return RestResponse.validfail(false, "上传分块失败");
        }
        log.debug("上传分块文件成功:{}",object);
        return RestResponse.success(true);
    }

    @Transactional
    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        // 合并分块
        String object = fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/chunk/";
        // 获取合并数据
        ArrayList<ComposeSource> composeSources = new ArrayList<>();
        for (int i = 0; i < chunkTotal; i++) {
            ComposeSource build = ComposeSource.builder().bucket(bucketOfVedio).object(object + i).build();
            composeSources.add(build);
        }
        String filename = uploadFileParamsDto.getFilename();
        String extension = getExtension(filename);
        //获取存储名称
        String objectOfMerge = fileMd5.substring(0, 1) + "/" + fileMd5.substring(1, 2) + "/" + fileMd5 + "/" + fileMd5 + extension ;
        ComposeObjectArgs build = ComposeObjectArgs.builder().bucket(bucketOfVedio).sources(composeSources).object(objectOfMerge).build();
        try {
            minioClient.composeObject(build);
            log.debug("合并文件成功：{}",objectOfMerge);
        } catch (Exception e) {
            log.debug("合并文件失败,fileMd5:{},异常:{}",fileMd5,e.getMessage(),e);
            return RestResponse.validfail(false, "合并文件失败。");
        }
        // 文件校验
        File file = downloadFileFromMinIO(bucketOfVedio, objectOfMerge);
        try (FileInputStream fileInputStream = new FileInputStream(file)){
            String md5Hex = DigestUtils.md5Hex(fileInputStream);
            if (!md5Hex.equals(fileMd5)){
                return RestResponse.validfail(false, "文件合并校验失败，最终上传失败。");
            }
            // 设置文件大小
            uploadFileParamsDto.setFileSize(file.length());
        }catch (Exception e){
            log.error("校验文件失败,fileMd5:{},异常:{}",fileMd5,e.getMessage(),e);
            return RestResponse.validfail(false, "文件合并校验失败，最终上传失败。");
        }
        // 将数据保存至数据库
        MediaFiles mediaFiles = addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucketOfVedio, objectOfMerge);
        if (mediaFiles == null){
            log.error("数据库插入失败");
            return RestResponse.validfail(false,"文件入库失败");
        }
//         删除分块文件
        for (int i = 0; i < chunkTotal; i++) {
            deleteBlock(bucketOfVedio,object + i);
        }
        return RestResponse.success(true);
    }

    /**
     * 获取文件的扩展名
     *
     * @param fileName 文件名
     * @return
     */
    public String getExtension(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf("."));
        return extension;
    }

    /**
     * 通过文件扩展名获取 mimeType
     *
     * @param extension
     * @return
     */
    public String getMimeType(String extension) {
        if (extension == null)
            extension = "";
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        //通用mimeType，字节流
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    /**
     * 将文件上传到 minio 文件管理系统
     *
     * @param mimeType 文件类型
     * @param bucket   bucket
     * @param filename 原始文件位置
     * @param object   在 minio 中的存储位置
     * @return
     */
    public boolean uploadFileToMinio(String mimeType, String bucket, String filename, String object) {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket).object(object)
                    .filename(filename)
                    .contentType(mimeType)
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
            log.info("文件上传成功，bucket:{},object:{},filename:{},mimeType:{},错误信息:{}", bucket, object, filename, mimeType);
            return true;
        } catch (Exception e) {
            log.info("文件上传失败，bucket:{},object:{},filename:{},mimeType:{},错误信息:{}", bucket, object, filename, mimeType, e.getMessage());
        }
        return false;
    }

    @Override
    public MediaFiles getMediaFileById(String mediaId) {
        MediaFiles mediaFile = mediaFilesMapper.selectById(mediaId);
        return mediaFile;
    }

    @Override
    public void removeMediaFile(String mediaId) {
        //查询 mediaFile 表中是否存在此媒体文件
        MediaFiles mediaFiles = mediaFilesMapper.selectById(mediaId);
        if (mediaFiles == null){
            EducationException.cast("此文件不存在");
        }
        //查询此媒体文件是否存在绑定信息  true:绑定  false：没有绑定
        boolean bind = contentServiceClient.isBind(mediaId);
        if (bind){
            EducationException.cast("此媒体文件绑定了相关课程，请先解绑课程后再次尝试");
        }
        //删除文件
        String bucket = mediaFiles.getBucket();
        String filePath = mediaFiles.getFilePath();
        deleteBlock(bucket,filePath);
        //删除对应记录
        mediaFilesMapper.deleteById(mediaId);
    }

    /**
     * 获取年月日
     *
     * @return yyyy/mm/dd/
     */
    public String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String folder = sdf.format(new Date()).replace("-", "/") + "/";
        return folder;
    }

    /**
     * 获取文件的 md5 值
     *
     * @param file
     * @return
     */
    public String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 保存数据至数据库
     * @param companyId 机构 id
     * @param fileMd5 文件 md5
     * @param uploadFileParamsDto 上传至数据库数据模型
     * @param bucket bucket
     * @param objectName object
     * @return
     */
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
        //从数据库查询文件
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            //拷贝基本信息
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditStatus("002003");
            mediaFiles.setStatus("1");
            //保存文件信息到文件表
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 0) {
                log.error("保存文件信息到数据库失败,{}", mediaFiles.toString());
                EducationException.cast("保存文件信息失败");
            }
            log.debug("保存文件信息到数据库成功,{}", mediaFiles.toString());
        }
        // 将视频文件保存到待处理表
        addWaitingTask(mediaFiles);
        return mediaFiles;

    }

    /**
     * 添加待处理任务
     * @param mediaFiles 媒资文件信息
     */
    private void addWaitingTask(MediaFiles mediaFiles){
        //获取视频类型 mimeType
        String extension = getExtension(mediaFiles.getFilename());
        String mimeType = getMimeType(extension);
        if(mimeType.equals("video/x-msvideo")){
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles,mediaProcess);
            mediaProcess.setStatus("1");//未处理
            mediaProcess.setFailCount(0);//失败次数默认为0
            mediaProcess.setUrl(null);
            mediaProcessMapper.insert(mediaProcess);
        }

    }

        /**
         * 从 minio 下载文件，用于文件校验
         * @param bucket
         * @param objectName
         * @return
         */
    public File downloadFileFromMinIO(String bucket,String objectName){
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try{
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile=File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream,outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 删除分块  将 minio 中的文件删除
     * @param bucket
     * @param objectName
     */
    public void deleteBlock(String bucket,String objectName){
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket(bucket)
                .object(objectName).build();
        try {
            minioClient.removeObject(removeObjectArgs);
            log.info("删除成功");
        } catch (Exception e) {
            log.info("删除失败");
        }
    }

    /**
     * 判断注入条件是否符合
     * @param item
     * @return
     */
    private  boolean isFill(String item){
        if (item != null && !item.equals("")){
            return true;
        }
        return false;
    }
}
