package com.photography.agent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * 摄影工具类 —— 通过 Spring AI @Tool 注解注册为 Function Calling 工具。
 * AI 模型在回答摄影问题时，会自动调用这些工具进行精确计算。
 */
@Component
public class PhotographyTools {

    private static final Logger log = LoggerFactory.getLogger(PhotographyTools.class);

    /**
     * 计算景深（Depth of Field）
     *
     * @param aperture         光圈值（如 2.8, 4, 5.6, 8, 11, 16）
     * @param focalLengthMm    焦距（mm）
     * @param subjectDistanceM 对焦距离（米）
     * @param circleOfConfusionMm 弥散圆直径（mm，全画幅默认 0.029）
     * @return 前景深、后景深、总景深的文本描述
     */
    @Tool(name = "calculateDoF", description = "Calculate depth of field given aperture, focal length, subject distance, and circle of confusion")
    public String calculateDepthOfField(
            double aperture,
            double focalLengthMm,
            double subjectDistanceM,
            Double circleOfConfusionMm) {

        double coc = circleOfConfusionMm != null ? circleOfConfusionMm : 0.029;
        double f = focalLengthMm;
        double s = subjectDistanceM * 1000; // 转为 mm
        double N = aperture;
        double c = coc;

        // 超焦距 hyperfocal distance
        double H = (f * f) / (N * c) + f;

        // 前景深
        double Dn = (H * s) / (H + (s - f));
        // 后景深
        double Df = (H * s) / (H - (s - f));
        // 总景深
        double dof = Df - Dn;

        // 转回米
        double Dn_m = Dn / 1000;
        double Df_m = Df / 1000;
        double dof_m = dof / 1000;
        double H_m = H / 1000;

        String result = String.format(""""
                景深计算结果（光圈 f/%.1f，焦距 %.0fmm，对焦距离 %.1fm）：
                - 超焦距：%.2fm
                - 前景深边界：%.2fm
                - 后景深边界：%.2fm
                - 总景深：%.2fm
                """, aperture, focalLengthMm, subjectDistanceM, H_m, Dn_m, Df_m, dof_m);

        log.debug("DoF calculated: aperture={}, focalLength={}mm, distance={}m -> {}m",
                aperture, focalLengthMm, subjectDistanceM, String.format("%.2f", dof_m));
        return result;
    }

    /**
     * 计算曝光值（Exposure Value）和等效曝光组合
     *
     * @param iso            ISO 感光度
     * @param aperture       光圈值
     * @param shutterSpeed   快门速度（秒，如 1/125 = 0.008）
     * @return 曝光值和等效组合建议
     */
    @Tool(name = "calculateExposure", description = "Calculate exposure value (EV) and suggest equivalent exposure combinations")
    public String calculateExposure(int iso, double aperture, double shutterSpeed) {
        // EV100 = log2(N^2 / t) - log2(ISO/100)
        double ev = Math.log(Math.pow(aperture, 2) / shutterSpeed) / Math.log(2);
        double ev100 = ev - Math.log(iso / 100.0) / Math.log(2);

        String result = String.format(""""
                曝光计算结果（ISO %d, f/%.1f, 1/%.0fs）：
                - EV（当前）：%.1f
                - EV100（归一化）：%.1f

                等效曝光组合（相同 EV100 ≈ %.1f）：
                - f/%.1f + 1/%d s（大光圈）
                - f/%.0f   + 1/%d s（中间值）
                - f/%.0f   + 1/%d s（小光圈）
                """, iso, aperture, 1 / shutterSpeed, ev, ev100, ev100,
                aperture, (int) (1 / shutterSpeed),
                Math.round(aperture * 1.4), (int) (1 / (shutterSpeed * 2)),
                Math.round(aperture * 2), (int) (1 / (shutterSpeed / 2)));

        log.debug("Exposure calculated: EV100={}", String.format("%.1f", ev100));
        return result;
    }

    /**
     * 阳光 16 法则 —— 估算正确曝光参数
     *
     * @param iso             ISO 感光度
     * @param lightingCondition 光照条件（bright-sunny / slight-overcast / overcast / heavy-overcast / sunset / indoor）
     * @return 推荐的光圈和快门速度
     */
    @Tool(name = "sunny16Rule", description = "Estimate correct exposure using the Sunny 16 rule")
    public String sunny16Rule(int iso, String lightingCondition) {
        // 光照条件对应光圈偏移（相对于 Sunny 16）
        int apertureOffset;
        String conditionDesc;

        switch (lightingCondition.toLowerCase()) {
            case "bright-sunny":
                apertureOffset = 0;
                conditionDesc = "晴朗（Sunny 16）";
                break;
            case "slight-overcast":
                apertureOffset = 1;
                conditionDesc = "轻微多云（f/11）";
                break;
            case "overcast":
                apertureOffset = 2;
                conditionDesc = "多云（f/8）";
                break;
            case "heavy-overcast":
                apertureOffset = 3;
                conditionDesc = "阴天（f/5.6）";
                break;
            case "sunset":
                apertureOffset = 4;
                conditionDesc = "日落（f/4）";
                break;
            case "indoor":
                apertureOffset = 6;
                conditionDesc = "室内（f/2.8）";
                break;
            default:
                apertureOffset = 0;
                conditionDesc = "晴朗（默认）";
        }

        double[] apertures = {16, 11, 8, 5.6, 4, 2.8, 2, 1.4};
        double aperture = apertures[Math.min(apertureOffset, apertures.length - 1)];
        double shutterSpeed = 1.0 / iso;

        return String.format(""""
                阳光 16 法则估算（ISO %d）：
                - 光照条件：%s
                - 推荐光圈：f/%.1f
                - 推荐快门：1/%d 秒
                - 建议：先试拍一张，根据直方图微调
                """, iso, conditionDesc, aperture, (int) shutterSpeed);
    }

    /**
     * 焦距等效换算（不同画幅之间）
     *
     * @param focalLengthMm 镜头实际焦距（mm）
     * @param fromSensor    来源画幅（full-frame / aps-c / micro-four-thirds / medium-format）
     * @return 等效全画幅焦距
     */
    @Tool(name = "focalLengthEquivalent", description = "Convert focal length between different sensor sizes")
    public String focalLengthEquivalent(double focalLengthMm, String fromSensor) {
        double cropFactor;
        String sensorName;

        switch (fromSensor.toLowerCase()) {
            case "full-frame":
                cropFactor = 1.0;
                sensorName = "全画幅";
                break;
            case "aps-c":
                cropFactor = 1.5;
                sensorName = "APS-C";
                break;
            case "aps-c-canon":
                cropFactor = 1.6;
                sensorName = "APS-C（佳能）";
                break;
            case "micro-four-thirds":
                cropFactor = 2.0;
                sensorName = "M4/3";
                break;
            case "medium-format":
                cropFactor = 0.79;
                sensorName = "中画幅";
                break;
            case "1-inch":
                cropFactor = 2.7;
                sensorName = "1英寸";
                break;
            default:
                cropFactor = 1.0;
                sensorName = "全画幅（未知格式默认）";
        }

        double equivalent = focalLengthMm * cropFactor;

        return String.format(""""
                焦距等效换算：
                - 镜头焦距：%.0fmm（%s）
                - 等效全画幅：%.0fmm
                - 换算系数：%.1fx
                """, focalLengthMm, sensorName, equivalent, cropFactor);
    }
}
