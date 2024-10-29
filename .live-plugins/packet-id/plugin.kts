import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.Messages
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.idea.search.allScope
import org.jetbrains.kotlin.idea.stubindex.KotlinFullClassNameIndex
import org.jetbrains.kotlin.psi.*
import liveplugin.registerAction

// depends-on-plugin org.jetbrains.kotlin

registerAction(
    id = "InsertPacketAction",
    actionGroupId = "GenerateGroup",
    action = InsertPacketAction()
)

class InsertPacketAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return

        // Prompt the user for the packet name
        val packetName: String = Messages.showInputDialog(
            project,
            "Enter new packet name:",
            "New Packet",
            Messages.getQuestionIcon()
        )?.trim() ?: return

        if (packetName.isEmpty()) {
            Messages.showErrorDialog(project, "Packet name cannot be empty", "Error")
            return
        }

        val packetIdStr = Messages.showInputDialog(
            project,
            "Enter packet ID (hexadecimal, e.g., 0x03):",
            "New Packet ID",
            Messages.getQuestionIcon()
        )?.trim() ?: return

        if (packetIdStr.isEmpty()) {
            Messages.showErrorDialog(project, "Packet ID cannot be empty", "Error")
            return
        }

        val packetId = packetIdStr.radixStringToIntOrNull() ?: run {
            Messages.showErrorDialog(project, "Invalid packet ID", "Error")
            return
        }

        // Find the 'DefaultIds' object using the fully qualified name
        val fqName = "dev.slne.surf.cloud.api.meta.DefaultIds"
        val defaultIdsObject = KotlinFullClassNameIndex[fqName, project, GlobalSearchScope.allScope(project)]
            .firstOrNull() as? KtObjectDeclaration ?: run {
            Messages.showErrorDialog(project, "Cannot find DefaultIds object.", "Error")
            return
        }

        WriteCommandAction.runWriteCommandAction(project) {
            val properties = defaultIdsObject.declarations.filterIsInstance<KtProperty>()
            val ktPsiFactory = KtPsiFactory(project)

            // Create the new property
            val newPropertyText = "const val $packetName = 0x${String.format("%02X", packetId)}"
            val newProperty = ktPsiFactory.createProperty(newPropertyText)

            // Initialize variables for insertion logic
            var idConflict = false
            var insertIndex = properties.size

            for ((i, property) in properties.withIndex()) {
                val initializer = property.initializer ?: continue
                val existingId = initializer.text.radixStringToIntOrNull() ?: continue

                if (existingId == packetId) {
                    idConflict = true
                }

                if (existingId >= packetId) {
                    insertIndex = i
                    break
                }
            }

            // Shift IDs of subsequent properties if there's a conflict
            if (idConflict) {
                for (i in properties.size - 1 downTo insertIndex) {
                    val property = properties[i]
                    val initializer = property.initializer ?: continue

                    val existingId = initializer.text.radixStringToIntOrNull() ?: continue
                    val newId = existingId + 1
                    val newInitializerText = "0x${String.format("%02X", newId)}"
                    val newInitializer = ktPsiFactory.createExpression(newInitializerText)
                    property.initializer = newInitializer
                }
            }

            // Insert the new property at the correct position
            if (insertIndex < properties.size) {
                defaultIdsObject.addBefore(newProperty, properties[insertIndex])
            } else {
                defaultIdsObject.add(newProperty)
            }
        }
    }

    private fun String.radixStringToIntOrNull(): Int? {
        return if (startsWith("0x")) {
            substring(2).toIntOrNull(16)
        } else {
            toIntOrNull()
        }
    }
}
