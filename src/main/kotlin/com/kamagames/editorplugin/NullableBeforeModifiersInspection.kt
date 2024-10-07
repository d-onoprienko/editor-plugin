package com.kamagames.editorplugin

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.codeInspection.*
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.refactoring.suggested.createSmartPointer

class NullableBeforeModifiersInspection : AbstractBaseJavaLocalInspectionTool() {

    companion object {
        const val NULLABLE_ANNOTATION = "javax.annotation.Nullable"
        const val PROBLEM_DESCRIPTION = "@Nullable should be placed before returning type"
    }

    override fun checkMethod(
        method: PsiMethod,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor> {
        val problemsHolder = ProblemsHolder(manager, method.containingFile, isOnTheFly)
        val nullableAnnotation = AnnotationUtil.findAnnotation(method, NULLABLE_ANNOTATION)
        if (nullableAnnotation != null && method.modifierList.lastChild != nullableAnnotation) {
            problemsHolder.registerProblem(
                method,
                PROBLEM_DESCRIPTION,
                PutNullableOnTheReturnTypeFix(method.createSmartPointer(), nullableAnnotation.createSmartPointer())
            )
        }
        return problemsHolder.resultsArray
    }
}

class PutNullableOnTheReturnTypeFix(
    private val methodPointer: SmartPsiElementPointer<PsiMethod>,
    private val nullableAnnotationPointer: SmartPsiElementPointer<PsiAnnotation>
) : LocalQuickFixOnPsiElement(methodPointer.element!!) {
    override fun getFamilyName(): String {
        return "Move @Nullable"
    }

    override fun getText(): String {
        return "Put @Nullable on the return type"
    }

    override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
        val method = methodPointer.element!!
        WriteCommandAction.runWriteCommandAction(
            project,
            "Put_@Nullable_on_the_return_type",
            "nullable_annotation_fixes",
            {
                val nullableAnnotation = nullableAnnotationPointer.element!!
                val copiedNullableAnnotation = nullableAnnotation.copy()
                nullableAnnotation.delete()
                method.returnTypeElement?.addBefore(
                    copiedNullableAnnotation,
                    method.returnTypeElement
                )
                CodeStyleManager.getInstance(project).reformat(method)
                JavaCodeStyleManager.getInstance(project).shortenClassReferences(copiedNullableAnnotation)
            },
            method.containingFile
        )

    }
}