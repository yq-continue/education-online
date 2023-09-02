package com.education.media.feiclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author yang
 * @create 2023-09-02 17:01
 */
@RequestMapping("/content")
@FeignClient(value = "content-api",fallbackFactory = ContentServiceClientFallback.class)
public interface ContentServiceClient {

    @GetMapping("/teachplan/media/{mediaId}")
    public boolean isBind(@PathVariable("mediaId")String mediaId);

}
