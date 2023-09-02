package com.education.media.feiclient;

import com.education.base.exception.EducationException;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author yang
 * @create 2023-09-02 17:05
 */
@Component
@Slf4j
public class ContentServiceClientFallback  implements FallbackFactory<ContentServiceClient> {
    @Override
    public ContentServiceClient create(Throwable throwable) {
        return new ContentServiceClient() {
            @Override
            public boolean isBind(String mediaId) {
                log.info("远程调用内容管理模块查询媒体资源绑定信息失败,mediaId:{}",mediaId);
                EducationException.cast("服务器内部异常，请稍后再尝试");
                return false;
            }
        };
    }
}
