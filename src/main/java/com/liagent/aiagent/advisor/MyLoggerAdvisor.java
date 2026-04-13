package com.liagent.aiagent.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

public class MyLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {
    private static final Logger log = LoggerFactory.getLogger(MyLoggerAdvisor.class);

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {

        advisedRequest = before(advisedRequest);
        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
        observeAfter(advisedResponse);
        return advisedResponse;
    }

    private void observeAfter(AdvisedResponse advisedResponse) {
        log.info(advisedResponse.response().getResult().getOutput().getText());
    }


    private AdvisedRequest before(AdvisedRequest advisedRequest) {
        log.info(advisedRequest.userText());
        return advisedRequest;
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        advisedRequest = before(advisedRequest);

        Flux<AdvisedResponse> advisedResponse = chain.nextAroundStream(advisedRequest);
        return new MessageAggregator().aggregateAdvisedResponse(advisedResponse, this::observeAfter);
    }

    @Override
    public String getName() {
        return "日志";
    }

    @Override
    public int getOrder() {
        return 100;
    }
}