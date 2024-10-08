package com.kamagames.inspectionplugin

import com.intellij.codeInspection.*
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.refactoring.suggested.createSmartPointer
import com.kamagames.inspectionplugin.Annotations.Companion.JAVAX_NULLABLE

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
                AddAnnotationOnTypeFix(JAVAX_NULLABLE, field.typeElement!!.createSmartPointer())
            )
        }
        return problemsHolder.resultsArray
    }
}

class AddAnnotationOnTypeFix(
    private val annotationToAdd: String,
    private val typePointer: SmartPsiElementPointer<PsiTypeElement>
) : LocalQuickFixOnPsiElement(typePointer.element!!) {
    override fun getFamilyName(): String {
        return "Put @Nullable"
    }

    override fun getText(): String {
        return "Put @Nullable on the type"
    }

    override fun invoke(project: Project, psiFile: PsiFile, startElement: PsiElement, endElement: PsiElement) {
        val typeElement = typePointer.element!!
        WriteCommandAction.runWriteCommandAction(
            typeElement.project,
            "Put_@Nullable_on_the_type",
            "nullable_annotation_fixes",
            {
                val addedAnnotation = typeElement.addAnnotation(annotationToAdd)
                JavaCodeStyleManager.getInstance(project).shortenClassReferences(addedAnnotation)
                CodeStyleManager.getInstance(project).reformat(typeElement.containingFile)
            },
            typeElement.containingFile
        )
    }

}