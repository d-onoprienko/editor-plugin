package com.kamagames.editorplugin

import com.intellij.codeInsight.intention.AddAnnotationFix
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifierListOwner
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager

class JsonPropertyWithoutNullableInspection : AbstractBaseJavaLocalInspectionTool() {

    companion object {
        const val JSON_PROPERTY_ANNOTATION = "com.fasterxml.jackson.annotation.JsonProperty"
        const val NULLABLE_ANNOTATION = "javax.annotation.Nullable"

        const val PROBLEM_DESCRIPTION = "Properties that are set by Jackson should be marked as @Nullable"
    }

    override fun checkMethod(
        method: PsiMethod,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor> {
        val problemsHolder = ProblemsHolder(manager, method.containingFile, isOnTheFly)
        if (method.isConstructor) {
            method.parameterList.parameters.forEach { parameter ->
                val jacksonAnnotation = parameter.getAnnotation(JSON_PROPERTY_ANNOTATION)
                if (jacksonAnnotation != null) {
                    val nullableAnnotation = parameter.getAnnotation(NULLABLE_ANNOTATION)
                    if (nullableAnnotation == null) {
                        problemsHolder.registerProblem(
                            parameter, PROBLEM_DESCRIPTION, AddAnnotationAfterAnotherFix(
                                NULLABLE_ANNOTATION, jacksonAnnotation, parameter
                            )
                        )
                    }
                }
            }
        }
        return problemsHolder.resultsArray
    }
}


class AddAnnotationAfterAnotherFix(
    val annotationToAdd: String,
    val anchorAnnotation: PsiAnnotation,
    val annotatedElement: PsiModifierListOwner
) : AddAnnotationFix(annotationToAdd, annotatedElement) {
    override fun applyFix() {
        val containingFile = annotatedElement.containingFile
        val project = containingFile.project
        WriteCommandAction.runWriteCommandAction(
            project,
            "Add_annotation_after_another",
            "annotation_fixes",
            {
                val modifierList = annotatedElement.modifierList
                val elementFactory = JavaPsiFacade.getElementFactory(project)
                val nullableAnnotation =
                    elementFactory.createAnnotationFromText("@$annotationToAdd", null)
                modifierList?.addAfter(nullableAnnotation, anchorAnnotation)
                CodeStyleManager.getInstance(project).reformat(annotatedElement)
                JavaCodeStyleManager.getInstance(project).shortenClassReferences(nullableAnnotation)
            },
            containingFile
        )
    }
}