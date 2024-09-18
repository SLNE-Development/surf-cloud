import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.Messages
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.search.GlobalSearchScope
import liveplugin.registerAction
import java.lang.String.format


// depends-on-plugin com.intellij.java

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

        val psiFacade = JavaPsiFacade.getInstance(project)
        val surfNettyPacketClass = psiFacade.findClass(
            "dev.slne.surf.cloud.api.meta.SurfNettyPacket",
            GlobalSearchScope.allScope(project)
        ) ?: run {
            Messages.showErrorDialog(project, "Cannot find SurfNettyPacket class.", "Error")
            return
        }

        val defaultIdsClass =
            surfNettyPacketClass.findInnerClassByName("DefaultIds", false) ?: run {
                Messages.showErrorDialog(project, "Cannot find DefaultIds class.", "Error")
                return
            }

        // Begin a write action to modify the PSI tree
        WriteCommandAction.runWriteCommandAction(project) {
            val fields = defaultIdsClass.fields
            val elementFactory = JavaPsiFacade.getElementFactory(project)

            // Create the new field
            val newFieldText = format("int %s = 0x%02X;", packetName, packetId)
            val newField = elementFactory.createFieldFromText(newFieldText, null)

            // Initialize variables for insertion logic
            var idConflict = false
            var insertIndex = fields.size

            for ((i, field) in fields.withIndex()) {
                val text = field.initializer?.text ?: continue
                val existingId = text.radixStringToIntOrNull() ?: continue

                if (existingId == packetId) {
                    idConflict = true
                }

                if (existingId >= packetId) {
                    insertIndex = i
                    break
                }
            }

            // Shift IDs of subsequent fields if there's a conflict
            if (idConflict) {
                for (i in fields.size - 1 downTo insertIndex) {
                    val field = fields[i]
                    val text = field.initializer?.text ?: continue

                    val existingId = text.radixStringToIntOrNull() ?: continue
                    val newId = existingId + 1
                    val newInitializerText = String.format("0x%02X", newId)
                    val newInitializer =
                        elementFactory.createExpressionFromText(newInitializerText, null)
                    field.initializer = newInitializer
                }
            }

            // Insert the new field at the correct position
            if (insertIndex < fields.size) {
                defaultIdsClass.addBefore(newField, fields[insertIndex]);
            } else {
                defaultIdsClass.add(newField);
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