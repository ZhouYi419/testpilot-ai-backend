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
         * 根据 Prompt 内容判断当前任务类型。
         */
        if (userPrompt != null && userPrompt.contains("【质量评审输出 JSON 格式】")) {
            return mockReviewResult();
        }

        if (userPrompt != null && userPrompt.contains("【缺失用例补全输出 JSON 格式】")) {
            return mockCompleteMissingResult();
        }

        return mockGenerateResult();
    }

    private String mockGenerateResult() {
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

    private String mockReviewResult() {
        return """
                {
                  "totalScore": 82,
                  "dimensions": [
                    {
                      "dimension": "主流程覆盖",
                      "score": 18,
                      "maxScore": 20,
                      "comment": "已覆盖支付成功后权益发放主流程"
                    },
                    {
                      "dimension": "异常流程覆盖",
                      "score": 16,
                      "maxScore": 20,
                      "comment": "已覆盖支付失败，但缺少未登录、订单超时等异常场景"
                    },
                    {
                      "dimension": "边界场景覆盖",
                      "score": 10,
                      "maxScore": 15,
                      "comment": "缺少重复点击、频繁下单、弱网等边界场景"
                    },
                    {
                      "dimension": "数据一致性",
                      "score": 14,
                      "maxScore": 15,
                      "comment": "已有订单和会员权益校验建议"
                    },
                    {
                      "dimension": "可执行性",
                      "score": 14,
                      "maxScore": 15,
                      "comment": "步骤清晰，预期结果明确"
                    },
                    {
                      "dimension": "来源可追溯",
                      "score": 10,
                      "maxScore": 15,
                      "comment": "已有来源引用，但可进一步绑定 chunkId"
                    }
                  ],
                  "missingPoints": [
                    {
                      "type": "异常测试",
                      "priority": "P1",
                      "description": "用户未登录时进入会员中心购买会员，应跳转登录页面",
                      "suggestion": "补充未登录购买会员的异常流程用例"
                    },
                    {
                      "type": "边界测试",
                      "priority": "P1",
                      "description": "用户短时间内频繁下单，需要触发风控限制",
                      "suggestion": "补充频繁下单风控限制用例"
                    },
                    {
                      "type": "异常测试",
                      "priority": "P1",
                      "description": "订单支付超时后应关闭订单且不发放权益",
                      "suggestion": "补充支付超时关闭订单用例"
                    }
                  ],
                  "duplicateCases": [],
                  "lowQualityCases": [],
                  "summary": "当前用例已覆盖核心支付成功和支付失败流程，但异常、边界和风控场景仍需补充。"
                }
                """;
    }

    private String mockCompleteMissingResult() {
        return """
                {
                  "testCases": [
                    {
                      "moduleName": "会员充值",
                      "caseTitle": "用户未登录时购买会员应跳转登录页面",
                      "caseType": "异常测试",
                      "priority": "P1",
                      "precondition": "用户未登录，会员商品配置正常",
                      "steps": [
                        "打开会员中心页面",
                        "选择月会员商品",
                        "点击立即购买"
                      ],
                      "expectedResult": "系统跳转登录页面，不创建支付订单，不发放会员权益",
                      "testData": {
                        "loginStatus": "NOT_LOGIN",
                        "productType": "月会员"
                      },
                      "sourceReferences": [
                        {
                          "versionNo": "v1.0",
                          "sectionTitle": "异常规则",
                          "sourceType": "REVIEW_MISSING_POINT"
                        }
                      ],
                      "riskPoint": "未登录用户绕过登录直接下单，可能导致订单归属异常",
                      "automationSuggestion": "可通过未登录态访问购买接口，断言返回未登录错误码"
                    },
                    {
                      "moduleName": "会员充值",
                      "caseTitle": "用户短时间频繁下单应触发风控限制",
                      "caseType": "边界测试",
                      "priority": "P1",
                      "precondition": "用户已登录，会员商品配置正常",
                      "steps": [
                        "进入会员中心",
                        "连续多次点击立即购买",
                        "观察订单创建结果和风控提示"
                      ],
                      "expectedResult": "系统限制频繁下单行为，返回明确风控提示，不应重复创建大量有效订单",
                      "testData": {
                        "clickCount": "多次",
                        "timeWindow": "短时间内"
                      },
                      "sourceReferences": [
                        {
                          "versionNo": "v1.0",
                          "sectionTitle": "风控规则",
                          "sourceType": "REVIEW_MISSING_POINT"
                        }
                      ],
                      "riskPoint": "频繁下单可能导致订单膨胀、支付状态错乱或风控失效",
                      "automationSuggestion": "可使用接口自动化模拟短时间重复提交并校验风控返回"
                    }
                  ]
                }
                """;
    }
}