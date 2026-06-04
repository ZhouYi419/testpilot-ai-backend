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
        if (userPrompt != null && userPrompt.contains("【影响分析输出 JSON 格式】")) {
            return mockImpactAnalysisResult();
        }

        if (userPrompt != null && userPrompt.contains("【增量测试用例输出 JSON 格式】")) {
            return mockIncrementalTestCaseResult();
        }

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

    private String mockImpactAnalysisResult() {
        return """
            {
              "changeSummary": [
                "会员充值模块新增连续包月能力",
                "新增首月优惠规则",
                "新增到期后自动续费逻辑"
              ],
              "affectedModules": [
                {
                  "moduleCode": "member_recharge",
                  "moduleName": "会员充值",
                  "impactLevel": "HIGH",
                  "reason": "新需求直接修改会员购买方式和续费规则"
                },
                {
                  "moduleCode": "order_payment",
                  "moduleName": "订单支付",
                  "impactLevel": "MEDIUM",
                  "reason": "连续包月涉及周期性扣费和支付状态处理"
                },
                {
                  "moduleCode": "member_rights",
                  "moduleName": "会员权益",
                  "impactLevel": "HIGH",
                  "reason": "自动续费成功或失败会影响会员权益发放和失效"
                }
              ],
              "relatedOldRules": [
                {
                  "versionNo": "v1.0",
                  "moduleCode": "member_recharge",
                  "rule": "用户可以购买月会员或年会员"
                },
                {
                  "versionNo": "v1.0",
                  "moduleCode": "member_recharge",
                  "rule": "支付成功后系统应立即发放会员权益"
                },
                {
                  "versionNo": "v1.0",
                  "moduleCode": "member_recharge",
                  "rule": "支付失败时不应发放会员权益"
                }
              ],
              "riskPoints": [
                {
                  "riskLevel": "HIGH",
                  "description": "自动续费扣费成功但会员权益未延长",
                  "suggestion": "需要校验支付成功回调和权益发放的一致性"
                },
                {
                  "riskLevel": "HIGH",
                  "description": "用户取消连续包月后仍被扣费",
                  "suggestion": "需要覆盖取消续费后的周期扣费验证"
                },
                {
                  "riskLevel": "MEDIUM",
                  "description": "首月优惠重复享受",
                  "suggestion": "需要覆盖同一用户重复开通、取消后再开通等场景"
                }
              ],
              "regressionScope": [
                {
                  "moduleCode": "member_recharge",
                  "moduleName": "会员充值",
                  "reason": "需要回归原有月会员、年会员购买流程"
                },
                {
                  "moduleCode": "member_rights",
                  "moduleName": "会员权益",
                  "reason": "需要回归权益发放、权益延长、权益失效逻辑"
                },
                {
                  "moduleCode": "order_payment",
                  "moduleName": "订单支付",
                  "reason": "需要回归支付成功、支付失败、支付超时场景"
                }
              ],
              "suggestedNewTestPoints": [
                {
                  "type": "功能测试",
                  "priority": "P0",
                  "description": "用户首次开通连续包月成功后享受首月优惠并获得会员权益"
                },
                {
                  "type": "异常测试",
                  "priority": "P0",
                  "description": "自动续费扣费失败后不应延长会员权益"
                },
                {
                  "type": "边界测试",
                  "priority": "P1",
                  "description": "用户取消连续包月后，到期不再自动扣费"
                },
                {
                  "type": "数据一致性",
                  "priority": "P0",
                  "description": "支付订单状态、订阅状态、会员权益有效期必须一致"
                }
              ],
              "summary": "该新需求会影响会员充值、订单支付和会员权益模块，重点测试连续包月开通、取消、自动续费、首月优惠和权益发放一致性。"
            }
            """;
    }

    private String mockIncrementalTestCaseResult() {
        return """
            {
              "testCases": [
                {
                  "moduleName": "会员充值",
                  "caseTitle": "用户首次开通连续包月成功后享受首月优惠并获得会员权益",
                  "caseType": "增量功能测试",
                  "priority": "P0",
                  "precondition": "用户已登录，用户从未开通过连续包月，连续包月商品配置正常",
                  "steps": [
                    "进入会员中心",
                    "选择连续包月商品",
                    "确认首月优惠价格",
                    "完成支付",
                    "返回会员中心查看会员状态和订阅状态"
                  ],
                  "expectedResult": "支付成功后订单状态为成功，用户享受首月优惠，会员权益立即生效，订阅状态为连续包月中",
                  "testData": {
                    "userType": "首次开通用户",
                    "productType": "连续包月",
                    "discountType": "首月优惠",
                    "payStatus": "SUCCESS"
                  },
                  "sourceReferences": [
                    {
                      "versionNo": "v1.1",
                      "sectionTitle": "新需求：连续包月",
                      "sourceType": "NEW_REQUIREMENT"
                    }
                  ],
                  "riskPoint": "支付成功但订阅状态或会员权益未正确更新",
                  "automationSuggestion": "可通过订单查询、订阅状态查询、会员权益查询三个接口做联合断言"
                },
                {
                  "moduleName": "会员权益",
                  "caseTitle": "连续包月自动续费扣费成功后会员权益有效期应自动延长",
                  "caseType": "增量功能测试",
                  "priority": "P0",
                  "precondition": "用户已开通连续包月，当前会员即将到期，自动续费扣费成功",
                  "steps": [
                    "构造会员即将到期状态",
                    "触发自动续费扣费成功回调",
                    "查询订单状态",
                    "查询会员权益有效期"
                  ],
                  "expectedResult": "系统生成续费订单，订单状态为支付成功，会员权益有效期自动延长一个周期",
                  "testData": {
                    "subscribeStatus": "ACTIVE",
                    "renewPayStatus": "SUCCESS"
                  },
                  "sourceReferences": [
                    {
                      "versionNo": "v1.1",
                      "sectionTitle": "新需求：自动续费",
                      "sourceType": "NEW_REQUIREMENT"
                    }
                  ],
                  "riskPoint": "扣费成功但权益未延长会导致用户投诉",
                  "automationSuggestion": "可模拟续费回调后校验会员有效期变化"
                },
                {
                  "moduleName": "会员充值",
                  "caseTitle": "回归原有月会员购买成功后权益立即生效",
                  "caseType": "回归测试",
                  "priority": "P0",
                  "precondition": "用户已登录，月会员商品配置正常",
                  "steps": [
                    "进入会员中心",
                    "选择月会员商品",
                    "完成支付",
                    "查看会员权益状态"
                  ],
                  "expectedResult": "原有月会员购买流程不受连续包月新需求影响，支付成功后权益仍立即生效",
                  "testData": {
                    "productType": "月会员",
                    "payStatus": "SUCCESS"
                  },
                  "sourceReferences": [
                    {
                      "versionNo": "v1.0",
                      "sectionTitle": "权益发放规则",
                      "sourceType": "OLD_VERSION_REGRESSION"
                    }
                  ],
                  "riskPoint": "新增连续包月后破坏原有月会员购买流程",
                  "automationSuggestion": "复用历史月会员购买自动化用例进行回归"
                }
              ]
            }
            """;
    }
}