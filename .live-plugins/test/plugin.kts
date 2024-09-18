import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.modcommand.ModPsiUpdater
import com.intellij.modcommand.PsiUpdateModCommandQuickFix
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiUtil
import com.siyeh.ig.fixes.RemoveModifierFix
import liveplugin.PluginUtil.show
import liveplugin.registerInspection

// depends-on-plugin com.intellij.java

show("Hello Kotlin world")

registerInspection(SurfNettyPacketHandlerInspection())

class SurfNettyPacketHandlerInspection : AbstractBaseJavaLocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : JavaElementVisitor() {
            override fun visitMethod(method: PsiMethod) {
                method.annotations.find { it.qualifiedName == "dev.slne.surf.cloud.api.meta.SurfNettyPacketHandler" } ?: return
                val params = method.parameterList.parameters

                if (params.isEmpty() || params.size > 2) {
                    holder.registerProblem(
                        method.parameterList,
                        "Listener method must have one or two parameters of type NettyPacket and optional NettyPacketInfo",
                        ProblemHighlightType.GENERIC_ERROR
                    )
                    return
                }

                val nettyPacket = "dev.slne.surf.cloud.api.netty.packet.NettyPacket"
                val nettyPacketInfo = "dev.slne.surf.cloud.core.netty.NettyPacketInfo"

                // only one nettypacket optional nettypacketinfo order is irrelevant
                if (params.size == 1) {
                    if (!params[0].extendsOrImplements(nettyPacket)) {
                        holder.registerProblem(
                            params[0].typeElement!!,
                            "Parameter must be of type NettyPacket",
                            ProblemHighlightType.GENERIC_ERROR
                        )
                    }
                } else {
                    if (!params[0].extendsOrImplements(nettyPacket) && !params[0].isClass(nettyPacketInfo)) {
                        holder.registerProblem(
                            params[0].typeElement!!,
                            "First parameter must be of type NettyPacket or NettyPacketInfo",
                            ProblemHighlightType.GENERIC_ERROR
                        )
                    }
                    if (!params[1].extendsOrImplements(nettyPacket) && !params[1].isClass(nettyPacketInfo)) {
                        holder.registerProblem(
                            params[1].typeElement!!,
                            "Second parameter must be of type NettyPacket or NettyPacketInfo",
                            ProblemHighlightType.GENERIC_ERROR
                        )
                    }
                }

                // check return type
                if (method.returnType?.canonicalText != "void") {
                    holder.registerProblem(
                        method.returnTypeElement!!,
                        "Listener method must have return type void",
                        ProblemHighlightType.GENERIC_ERROR
                    )
                }

                if (method.hasModifierProperty(PsiModifier.PRIVATE)) {
                    holder.registerProblem(
                        method.nameIdentifier!!,
                        "Listener method must not be private",
                        ProblemHighlightType.GENERIC_ERROR,
                        object: PsiUpdateModCommandQuickFix() {
                            override fun getFamilyName() = "Make method public"
                            override fun getName() = "Make method public"

                            override fun applyFix(project: Project, modifierElement: PsiElement, updater: ModPsiUpdater) {
                                val psiMethod = modifierElement.parent as? PsiMethod ?: return
                                psiMethod.modifierList.setModifierProperty(PsiModifier.PRIVATE, false)
                                psiMethod.modifierList.setModifierProperty(PsiModifier.PUBLIC, true)
                            }

                        }
                    )
                }
            }
        }
    }

    private fun PsiParameter.extendsOrImplements(className: String): Boolean {
        val findClass =
            JavaPsiFacade.getInstance(project).findClass(className, resolveScope) ?: return false
        return PsiUtil.resolveClassInClassTypeOnly(type)?.isInheritor(findClass, true) ?: false
    }

    private fun PsiParameter.isClass(className: String): Boolean {
        return type.canonicalText == className
    }
}