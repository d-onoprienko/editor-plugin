package com.kamagames.editorplugin

import com.intellij.codeInsight.intention.AddAnnotationFix
import com.intellij.codeInspection.*
import com.intellij.psi.PsiField

class NullableBeforeBoxedTypeInspection : AbstractBaseJavaLocalInspectionTool() {

    companion object {
        private const val NULLABLE_ANNOTATION = "javax.annotation.Nullable"
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
    }

    override fun checkField(
        field: PsiField,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor> {
        val problemsHolder = ProblemsHolder(manager, field.containingFile, isOnTheFly)
        if (BOXED_TYPES.contains(field.type.canonicalText) && !field.hasAnnotation(NULLABLE_ANNOTATION)) {
            problemsHolder.registerProblem(
                field,
                PROBLEM_DESCRIPTION,
                AddAnnotationFix(NULLABLE_ANNOTATION, field)
            )
        }
        return problemsHolder.resultsArray
    }
}