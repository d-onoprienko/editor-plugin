package com.kamagames.editorplugin

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiMethod

class NullableBeforeAccessModifiersInspection : AbstractBaseJavaLocalInspectionTool() {

    companion object {
        const val NULLABLE_ANNOTATION = "javax.annotation.Nullable"
    }

    override fun checkMethod(
        method: PsiMethod,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor> {
        val problemsHolder = ProblemsHolder(manager, method.containingFile, isOnTheFly)
        if (method.hasAnnotation(NULLABLE_ANNOTATION)) {
            val modifierList = method.modifierList
            modifierList.navigationElement
        }
        return problemsHolder.resultsArray
    }
}