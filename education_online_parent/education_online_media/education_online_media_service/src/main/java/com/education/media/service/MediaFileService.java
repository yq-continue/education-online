package com.education.media.service;

import com.education.base.model.PageParams;
import com.education.base.model.PageResult;
import com.education.media.model.dto.QueryMediaParamsDto;
import com.education.media.model.dto.UploadFileParamsDto;
import com.education.media.model.dto.UploadFileResultDto;
import com.education.media.model.po.MediaFiles;
import com.education.media.model.po.RestResponse;

import java.io.File;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理业务类
 * @date 2022/9/10 8:55
 */
public interface MediaFileService {

    /**
     * 媒资文件查询方法
     * @param companyId
     * @param pageParams
     * @param queryMediaParamsDto
     * @return
     */
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    /**
     * 文件上传
     * @param companyId
     * @param uploadFileParamsDto
     * @param localFilePath
     * @return
     */
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath,String objectName);

    /**
     * 检查文件是否存在于数据库
     * @param fileMd5 传输的文件的 md5 值
     * @return true ：文件存在   false：文件不存在
     */
    public RestResponse<Boolean> checkFile(String fileMd5);

    /**
     * 检查传输的分块是否存在 minion
     * @param fileMd5 文件的 md5 值
     * @param chunkIndex 分块标签
     * @return true ：文件存在   false：文件不存在  return RestResponse.success(true);
     */
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

    /**
     * 上传文件
     * @param fileMd5 文件的 md5 值
     * @param chunk 分块值
     * @param localChunkFilePath 分块文件本地路径
     * @return true:文件上传成功  false：文件上传失败     RestResponse.validfail(false, "上传分块失败")
     */
    public RestResponse uploadChunk(String fileMd5,int chunk,String localChunkFilePath);

    /**
     * 合并分块
     * @param companyId 机构 id
     * @param fileMd5   file md5 值
     * @param chunkTotal    总文件数
     * @param uploadFileParamsDto   封装至数据库的数据
     * @return
     */
    public RestResponse mergechunks(Long companyId,String fileMd5,int chunkTotal,UploadFileParamsDto uploadFileParamsDto);

    /**
     * 从 minio 下载文件，用于文件校验
     * @param bucket
     * @param objectName
     * @return
     */
    public File downloadFileFromMinIO(String bucket, String objectName);

    /**
     * 将文件上传到 minio 文件管理系统
     *
     * @param mimeType 文件类型
     * @param bucket   bucket
     * @param filename 原始文件位置
     * @param object   在 minio 中的存储位置
     * @return
     */
    public boolean uploadFileToMinio(String mimeType, String bucket, String filename, String object);

    /**
     * 通过媒体 id 查询媒体数据
     * @param mediaId 媒体 id
     * @return
     */
    public MediaFiles getMediaFileById(String mediaId);

    /**
     * 移除媒体文件
     */
    public void removeMediaFile(String mediaId);



}
