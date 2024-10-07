package com.kamagames.editorplugin

import com.intellij.codeInsight.intention.AddAnnotationFix
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiField
import com.kamagames.editorplugin.Annotations.Companion.JAVAX_NULLABLE

class NullableBeforeBoxedTypeInspection : AbstractBaseJavaLocalInspectionTool() {

    override fun checkField(
        field: PsiField,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor> {
        val problemsHolder = ProblemsHolder(manager, field.containingFile, isOnTheFly)
        if (BOXED_TYPES.contains(field.type.canonicalText) && !field.hasAnnotation(JAVAX_NULLABLE)) {
            problemsHolder.registerProblem(
                field,
                PROBLEM_DESCRIPTION,
                AddAnnotationFix(JAVAX_NULLABLE, field)
            )
        }
        return problemsHolder.resultsArray
    }
}

private const val PROBLEM_DESCRIPTION = "Boxed types shoul be annotated Nullable"
private val BOXED_TYPES = setOf(
    "java.lang.Boolean",
    "java.lang.Integer",
    "java.lang.Long",
    "java.lang.Byte",
    "java.lang.Short",
    "java.lang.Double",
    "java.lang.Float",
    "java.lang.Character",
)