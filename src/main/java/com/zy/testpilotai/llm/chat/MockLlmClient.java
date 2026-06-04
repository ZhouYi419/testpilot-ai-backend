package com.zy.testpilotai.llm.chat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ai.chat.provider", havingValue = "mock", matchIfMissing = true)
public class MockLlmClient implements LlmClient {

    /**
     * Mock 模型名称。
     */
    @Value("${ai.chat.mock.model-name:mock-qwen-plus}")
    private String modelName;

    @Override
    public String modelName() {
        return modelName;
    }

    @Override
    public String chat(String systemPrompt, String userPrompt) {
        /*
         * Mock LLM 返回固定 JSON。
         */
        return """
                {
                  "testCases": [
                    {
                      "moduleName": "会员充值",
                      "caseTitle": "用户购买月会员支付成功后会员权益立即生效",
                      "caseType": "功能测试",
                      "priority": "P0",
                      "precondition": "用户已登录，账户状态正常，会员商品配置正常",
                      "steps": [
                        "进入会员中心",
                        "选择月会员商品",
                        "点击立即购买",
                        "完成支付",
                        "返回会员中心查看会员状态"
                      ],
                      "expectedResult": "订单支付成功，系统立即发放会员权益，会员状态变为有效",
                      "testData": {
                        "userType": "普通用户",
                        "productType": "月会员",
                        "payStatus": "SUCCESS"
                      },
                      "sourceReferences": [
                        {
                          "versionNo": "v1.0",
                          "sectionTitle": "权益发放规则",
                          "sourceType": "RAG"
                        }
                      ],
                      "riskPoint": "支付成功但权益未及时发放，导致用户无法使用会员能力",
                      "automationSuggestion": "可通过订单查询接口和会员权益查询接口进行自动化断言"
                    },
                    {
                      "moduleName": "会员充值",
                      "caseTitle": "用户支付失败后不应发放会员权益",
                      "caseType": "异常测试",
                      "priority": "P0",
                      "precondition": "用户已登录，会员商品配置正常",
                      "steps": [
                        "进入会员中心",
                        "选择月会员商品",
                        "点击立即购买",
                        "模拟支付失败",
                        "返回会员中心查看会员状态"
                      ],
                      "expectedResult": "订单状态为支付失败，系统不发放会员权益，会员状态不变",
                      "testData": {
                        "userType": "普通用户",
                        "productType": "月会员",
                        "payStatus": "FAILED"
                      },
                      "sourceReferences": [
                        {
                          "versionNo": "v1.0",
                          "sectionTitle": "异常规则",
                          "sourceType": "RAG"
                        }
                      ],
                      "riskPoint": "支付失败却发放权益，会造成资损",
                      "automationSuggestion": "可通过模拟支付失败回调后校验订单状态和会员权益状态"
                    }
                  ]
                }
                """;
    }
}