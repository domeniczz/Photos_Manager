package com.domenic.conditions;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.domenic.ImageMagick;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

/**
 * @author Domenic
 * @Classname EnabledIfImageMagickAvailableCondition
 * @Description 
 * @Date 3/24/2023 9:41 PM
 * @Created by Domenic
 */
public class EnabledIfImageMagickIsAvailableCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        return findAnnotation(extensionContext.getElement(), EnabledIfImageMagickIsAvailable.class)
                .map((annotation) -> (new ImageMagick().detectVersion() != ImageMagick.Version.NA)
                        ? ConditionEvaluationResult.enabled("ImageMagick Available.")
                        : ConditionEvaluationResult.disabled("No ImageMagick Available."))
                .orElse(ConditionEvaluationResult.disabled("By default, Imagemagick tests are disabled"));
    }

}
