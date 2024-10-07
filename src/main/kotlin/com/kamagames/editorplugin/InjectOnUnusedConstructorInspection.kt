package com.kamagames.editorplugin

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.codeInsight.daemon.impl.quickfix.ModifierFix
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.search.searches.MethodReferencesSearch
import com.kamagames.editorplugin.Annotations.Companion.GUICE_INJECT
import com.kamagames.editorplugin.Annotations.Companion.JAKARTA_INJECT
import com.kamagames.editorplugin.Annotations.Companion.JAVAX_INJECT

private const val PROBLEM_DESCRIPTION = "Unused constructors annotated with @Inject should be declared private"


class InjectOnUnusedConstructorInspection : AbstractBaseJavaLocalInspectionTool() {

    override fun checkMethod(
        method: PsiMethod,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor> {
        val problemsHolder = ProblemsHolder(manager, method.containingFile, isOnTheFly)
        if (method.isConstructor) {
            if (isNotUsed(method) && isAnnotatedInject(method) && isPublic(method)) {
                problemsHolder.registerProblem(
                    method,
                    PROBLEM_DESCRIPTION,
                    ModifierFix(method, PsiModifier.PRIVATE, true, true)
                )
            }
        }
        return problemsHolder.resultsArray
    }

    private fun isNotUsed(method: PsiMethod): Boolean {
        return MethodReferencesSearch.search(method).findAll().isEmpty()
    }

    private fun isAnnotatedInject(method: PsiMethod): Boolean {
        return AnnotationUtil.isAnnotated(method, JAVAX_INJECT, 0) ||
                AnnotationUtil.isAnnotated(method, JAKARTA_INJECT, 0) ||
                AnnotationUtil.isAnnotated(method, GUICE_INJECT, 0)
    }

    private fun isPublic(method: PsiMethod): Boolean {
        return method.modifierList.hasModifierProperty(PsiModifier.PUBLIC)
    }
}
