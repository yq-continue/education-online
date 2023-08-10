package com.education.media.service;

import com.education.media.model.po.MediaProcess;

import java.util.List;

/**
 * @author yang
 * @create 2023-08-05 15:18
 */
public interface MediaFileProcessService {

    /**
     * 查询所需要处理的数据
     * @param shardIndex
     * @param shardTotal
     * @param count
     * @return
     */
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);

    /**
     *任务执行完成保存任务信息
     * @param taskId  任务id
     * @param status  任务状态
     * @param fileId  文件 ID
     * @param url   执行任务后新的 url
     * @param errorMsg 错误信息
     */
    public void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);

    /**
     * 开启任务
     * @param id 任务 id
     * @return
     */
    public boolean startTask(long id);


}
