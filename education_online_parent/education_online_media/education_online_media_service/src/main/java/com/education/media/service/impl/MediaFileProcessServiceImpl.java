package com.education.media.service.impl;

import com.education.media.mapper.MediaFilesMapper;
import com.education.media.mapper.MediaProcessHistoryMapper;
import com.education.media.mapper.MediaProcessMapper;
import com.education.media.model.po.MediaFiles;
import com.education.media.model.po.MediaProcess;
import com.education.media.model.po.MediaProcessHistory;
import com.education.media.service.MediaFileProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yang
 * @create 2023-08-05 15:19
 */
@Service
@Slf4j
public class MediaFileProcessServiceImpl implements MediaFileProcessService {

    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);
        return mediaProcesses;
    }

    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        //要更新的任务
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if(mediaProcess == null){
            return ;
        }
        //如果任务执行失败
        if(status.equals("3")){
            //更新MediaProcess表的状态
            mediaProcess.setStatus("3");
            mediaProcess.setFailCount(mediaProcess.getFailCount()+1);//失败次数加1
            mediaProcess.setErrormsg(errorMsg);
            mediaProcessMapper.updateById(mediaProcess);
            return;

        }
        //======如果任务执行成功======
        //文件表记录
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        //更新media_file表中的url
        mediaFiles.setUrl(url);
        mediaFilesMapper.updateById(mediaFiles);

        //更新MediaProcess表的状态
        mediaProcess.setStatus("2");
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcess.setUrl(url);
        mediaProcessMapper.updateById(mediaProcess);

        //将MediaProcess表记录插入到MediaProcessHistory表
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess,mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);

        //从MediaProcess删除当前任务
        mediaProcessMapper.deleteById(taskId);

    }

    @Override
    public boolean startTask(long id) {
        int i = mediaProcessMapper.startTask(id);
        boolean isOpen = i <= 0 ? false : true;
        return isOpen;
    }
}
