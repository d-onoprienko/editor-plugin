package com.kamagames.inspectionplugin

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
import com.kamagames.inspectionplugin.Annotations.Companion.JAVAX_NULLABLE
import com.kamagames.inspectionplugin.Annotations.Companion.JSON_PROPERTY

private const val PROBLEM_DESCRIPTION = "Properties that are set by Jackson should be marked as @Nullable"


class JsonPropertyWithoutNullableInspection : AbstractBaseJavaLocalInspectionTool() {

    override fun checkMethod(
        method: PsiMethod,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor> {
        val problemsHolder = ProblemsHolder(manager, method.containingFile, isOnTheFly)
        if (method.isConstructor) {
            method.parameterList.parameters.forEach { parameter ->
                val jacksonAnnotation = parameter.getAnnotation(JSON_PROPERTY)
                if (jacksonAnnotation != null) {
                    val nullableAnnotation = parameter.getAnnotation(JAVAX_NULLABLE)
                    if (parameter.type !is PsiPrimitiveType && nullableAnnotation == null) {
                        problemsHolder.registerProblem(
                            parameter, PROBLEM_DESCRIPTION, AddAnnotationAfterAnotherFix(
                                JAVAX_NULLABLE,
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