package com.quickstart.draw.job;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickstart.common.domain.ErrorCode;
import com.quickstart.common.exception.BusinessException;
import com.quickstart.draw.config.XxlJobConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "xxl.job", name = "enabled", havingValue = "true")
public class XxlJobAdminClient {

    private static final int XXL_JOB_SUCCESS_CODE = 200;
    private static final DateTimeFormatter JOB_DESC_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final XxlJobConfig xxlJobConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public XxlJobAdminClient(XxlJobConfig xxlJobConfig,
                             RestTemplate xxlJobRestTemplate,
                             ObjectMapper objectMapper) {
        this.xxlJobConfig = xxlJobConfig;
        this.restTemplate = xxlJobRestTemplate;
        this.objectMapper = objectMapper;
    }

    public Integer addAndStartOpenDrawJob(Long drawId, LocalDateTime triggerTime) {
        String loginCookie = login();
        Integer jobId = addOpenDrawJob(drawId, triggerTime, loginCookie);
        startJob(jobId, loginCookie);
        log.info("XXL-JOB 开奖任务创建并启动成功, drawId={}, jobId={}, triggerTime={}",
                drawId, jobId, triggerTime);
        return jobId;
    }

    public void stopJob(Long jobId) {
        if (jobId == null) {
            return;
        }
        String loginCookie = login();
        stopJob(jobId, loginCookie);
        log.info("XXL-JOB 任务停止成功, jobId={}", jobId);
    }

    public void removeJob(Integer jobId) {
        if (jobId == null) {
            return;
        }
        String loginCookie = login();
        removeJob(jobId, loginCookie);
        log.info("XXL-JOB 任务删除成功, jobId={}", jobId);
    }

    private String login() {
        String url = adminBaseUrl() + "/login";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("userName", xxlJobConfig.getAdmin().getUsername());
        form.add("password", xxlJobConfig.getAdmin().getPassword());
        form.add("ifRemember", "on");

        ResponseEntity<String> response = postForm(url, form, null);
        assertSuccess(response.getBody(), "XXL-JOB 登录失败");

        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (cookies == null || cookies.isEmpty()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "XXL-JOB 登录未返回 Cookie");
        }

        return cookies.get(0);
    }

    private Integer addOpenDrawJob(Long drawId, LocalDateTime triggerTime, String loginCookie) {
        String url = adminBaseUrl() + "/jobinfo/add";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("jobGroup", String.valueOf(xxlJobConfig.getAdmin().getJobGroupId()));
        form.add("jobDesc", "抽签自动开奖-" + drawId + "-" + triggerTime.format(JOB_DESC_TIME_FORMATTER));
        form.add("author", "qs-draw");
        form.add("alarmEmail", "");
        form.add("scheduleType", "CRON");
        form.add("scheduleConf", buildOneShotCron(triggerTime));
        form.add("misfireStrategy", "DO_NOTHING");
        form.add("executorRouteStrategy", "FIRST");
        form.add("executorHandler", xxlJobConfig.getExecutor().getJobHandler());
        form.add("executorParam", String.valueOf(drawId));
        form.add("executorBlockStrategy", "SERIAL_EXECUTION");
        form.add("executorTimeout", "0");
        form.add("executorFailRetryCount", "1");
        form.add("glueType", "BEAN");
        form.add("glueRemark", "GLUE代码初始化");
        form.add("glueSource", "");
        form.add("childJobId", "");

        ResponseEntity<String> response = postForm(url, form, loginCookie);
        Map<String, Object> body = assertSuccess(response.getBody(), "XXL-JOB 创建开奖任务失败");

        Object content = body.get("content");
        if (content == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "XXL-JOB 创建任务未返回 jobId");
        }
        return Integer.valueOf(String.valueOf(content));
    }

    private void startJob(Integer jobId, String loginCookie) {
        String url = adminBaseUrl() + "/jobinfo/start";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("id", String.valueOf(jobId));

        ResponseEntity<String> response = postForm(url, form, loginCookie);
        assertSuccess(response.getBody(), "XXL-JOB 启动开奖任务失败");
    }

    private void stopJob(Long jobId, String loginCookie) {
        String url = adminBaseUrl() + "/jobinfo/stop";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("id", String.valueOf(jobId));

        ResponseEntity<String> response = postForm(url, form, loginCookie);
        assertSuccess(response.getBody(), "XXL-JOB 停止开奖任务失败");
    }

    private void removeJob(Integer jobId, String loginCookie) {
        String url = adminBaseUrl() + "/jobinfo/remove";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("id", String.valueOf(jobId));

        ResponseEntity<String> response = postForm(url, form, loginCookie);
        assertSuccess(response.getBody(), "XXL-JOB 删除开奖任务失败");
    }

    private ResponseEntity<String> postForm(String url, MultiValueMap<String, String> form, String cookie) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        if (cookie != null && !cookie.isBlank()) {
            headers.add(HttpHeaders.COOKIE, cookie);
        }
        return restTemplate.postForEntity(url, new HttpEntity<>(form, headers), String.class);
    }

    private Map<String, Object> assertSuccess(String responseBody, String errorMessage) {
        try {
            Map<String, Object> body = objectMapper.readValue(
                    responseBody,
                    new TypeReference<Map<String, Object>>() {
                    });
            Object code = body.get("code");
            if (code == null || Integer.parseInt(String.valueOf(code)) != XXL_JOB_SUCCESS_CODE) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                        errorMessage + "：" + body.getOrDefault("msg", responseBody));
            }
            return body;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    errorMessage + "，响应解析失败：" + responseBody);
        }
    }

    private String buildOneShotCron(LocalDateTime triggerTime) {
        return String.format("%d %d %d %d %d ? %d",
                triggerTime.getSecond(),
                triggerTime.getMinute(),
                triggerTime.getHour(),
                triggerTime.getDayOfMonth(),
                triggerTime.getMonthValue(),
                triggerTime.getYear());
    }

    private String adminBaseUrl() {
        String addresses = xxlJobConfig.getAdmin().getAddresses();
        if (addresses == null || addresses.isBlank()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "XXL-JOB Admin 地址未配置");
        }
        return addresses.split(",")[0].replaceAll("/+$", "");
    }
}
