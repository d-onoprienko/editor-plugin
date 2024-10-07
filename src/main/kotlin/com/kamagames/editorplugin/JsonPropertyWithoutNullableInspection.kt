package com.kamagames.editorplugin

import com.intellij.codeInsight.intention.AddAnnotationFix
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.refactoring.suggested.createSmartPointer

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
                    if (parameter.type !is PsiPrimitiveType && nullableAnnotation == null) {
                        problemsHolder.registerProblem(
                            parameter, PROBLEM_DESCRIPTION, AddAnnotationAfterAnotherFix(
                                NULLABLE_ANNOTATION,
                                jacksonAnnotation.createSmartPointer(),
                                parameter.createSmartPointer()
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
    val anchorAnnotationPointer: SmartPsiElementPointer<PsiAnnotation>,
    val annotatedElementPointer: SmartPsiElementPointer<PsiModifierListOwner>
) : AddAnnotationFix(annotationToAdd, annotatedElementPointer.element!!) {
    override fun applyFix() {
        val containingFile = annotatedElementPointer.element?.containingFile
        val project = containingFile?.project!!
        WriteCommandAction.runWriteCommandAction(
            project,
            "Add_annotation_after_another",
            "annotation_fixes",
            {
                val modifierList = annotatedElementPointer.element?.modifierList
                val elementFactory = JavaPsiFacade.getElementFactory(project)
                val nullableAnnotation =
                    elementFactory.createAnnotationFromText("@$annotationToAdd", null)
                modifierList?.addAfter(nullableAnnotation, anchorAnnotationPointer.element)
                CodeStyleManager.getInstance(project).reformat(annotatedElementPointer.element!!)
                JavaCodeStyleManager.getInstance(project).shortenClassReferences(nullableAnnotation)
            },
            containingFile
        )
    }
}