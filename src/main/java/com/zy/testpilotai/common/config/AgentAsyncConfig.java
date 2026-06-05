package com.zy.testpilotai.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AgentAsyncConfig {

    /**
     * Agent 异步任务线程池。
     */
    @Bean("agentTaskExecutor")
    public Executor agentTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(2);

        // 最大线程数：同时最多跑 4 个 Agent
        executor.setMaxPoolSize(4);

        // 队列容量：最多排队 100 个任务
        executor.setQueueCapacity(100);

        // 线程名前缀
        executor.setThreadNamePrefix("agent-task-");

        // 应用关闭时等待任务执行完成
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 最多等待 60 秒
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();
        return executor;
    }
}